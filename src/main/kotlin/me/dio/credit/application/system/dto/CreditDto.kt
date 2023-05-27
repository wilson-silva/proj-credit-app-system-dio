package me.dio.credit.application.system.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal
import java.time.LocalDate

data class CreditDto(
    @field:NotNull(message = "Invalid input") val creditValue: BigDecimal,
    @field: Future val dayFirstInstallments: LocalDate,
    @field:NotNull(message = "Invalid input") val numberOfInstallments: Int,
    @field:NotNull(message = "Invalid input") var customerId: Long

) {

    fun toEntity(): Credit = Credit(
        creditValue = this.creditValue,
        dayFirstInstallments = this.dayFirstInstallments,
        numberOfInstallments = this.numberOfInstallments,
        customer = Customer(id = this.customerId)
    )

}
