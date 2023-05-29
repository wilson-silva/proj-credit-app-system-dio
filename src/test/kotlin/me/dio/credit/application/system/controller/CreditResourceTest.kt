package me.dio.credit.application.system.controller


import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.CustomerServiceTest
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() {
        creditRepository.deleteAll()

    }


    @AfterEach
    fun tearDown(){
        creditRepository.deleteAll()

    }

    @Test
    fun `should create a credit and return 201 status`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val creditDto: CreditDto = builderCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.creditValue").value(1000))
            .andExpect(jsonPath("$.numberOfInstallment").value(15))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.emailCustomer").value("wilson@gmail.com"))
            .andExpect(jsonPath("$.incomeCustomer").value(1000))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a credit with customerId invalid and return 404 status`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val creditDto: CreditDto = builderCreditDto(customerId = 2L)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.title").value("Not Found! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(
                jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a credit when numberOfInstallments is less than zero and greater than 48 and returns status 400`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val creditDto: CreditDto = builderCreditDto(numberOfInstallments = 49)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .content(valueAsString)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find list credit by customer id and return 200 status`() {
        val customer: Customer = customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].creditCode").value(credit.creditCode.toString()))
            .andExpect(jsonPath("$[0].creditValue").value(1000))
            .andExpect(jsonPath("$[0].numberOfInstallments").value(15))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return empty credit list when fetching for invalid customer id and returning 200 status`() {
        val customer: Customer = customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL?customerId=${2L}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", Matchers.hasSize<Any>(0)))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find credit by creditCode and return 200 status`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${credit.creditCode.toString()}?customerId=${1L}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.creditValue").value(1000))
            .andExpect(jsonPath("$.numberOfInstallment").value(15))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.emailCustomer").value("wilson@gmail.com"))
            .andExpect(jsonPath("$.incomeCustomer").value(1000))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find credit by creditCode when customer id does not belong to credit and returns 400 status`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${credit.creditCode.toString()}?customerId=${2L}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bas Request! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.exception")
                    .value("class java.lang.IllegalArgumentException")
            )
            .andExpect(jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find credit by creditCode when creditCode not found and returns 404 status`() {
        customerRepository.save(CustomerServiceTest.buildCustomer())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())

        mockMvc.perform(
            MockMvcRequestBuilders.get("$URL/${UUID.randomUUID().toString()}?customerId=${1L}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.title").value("Not Found! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(
                jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            )
            .andExpect(jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }


    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(1000.0),
        dayFirstInstallments: LocalDate = LocalDate.parse("2023-07-25"),
        numberOfInstallments: Int = 15,
        customerId: Long = 1L
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstInstallments = dayFirstInstallments,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )

}