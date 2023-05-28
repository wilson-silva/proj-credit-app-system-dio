package me.dio.credit.application.system.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import me.dio.credit.application.system.dto.CustomerDto
import me.dio.credit.application.system.dto.CustomerUpdateDto
import me.dio.credit.application.system.dto.CustomerView
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.service.impl.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Resource (Controller)")
class CustomerResource(
    private val customerService: CustomerService
) {

    @Operation(summary = "create a customer", description = "Returns 201 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successful Operation")
        ]
    )
    @PostMapping
    fun saveCustomer(@RequestBody @Valid customerDto: CustomerDto): ResponseEntity<String> {
        val savedCustomer = this.customerService.save(customerDto.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).body("Customer ${savedCustomer.email} saved!")
    }

    @Operation(summary = "returns a customer by id", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
            ApiResponse(responseCode = "404", description = "Such a customer does not exist"),
        ]
    )
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<CustomerView> {
        val customer: Customer = this.customerService.findById(id)
        return ResponseEntity.status(HttpStatus.OK).body(CustomerView(customer))
    }

    @Operation(summary = "delete a customer by id", description = "Returns 204 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Successful Operation"),
            ApiResponse(responseCode = "404", description = "Such a customer does not exist"),
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: Long) = this.customerService.delete(id);

    @Operation(summary = "update a customer by id", description = "Returns 200 if successful")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful Operation"),
            ApiResponse(responseCode = "404", description = "Such a customer does not exist"),
        ]
    )
    @PatchMapping
    fun updateCustomer(
        @RequestParam(value = "customerId") id: Long,
        @RequestBody @Valid customerUpdateDto: CustomerUpdateDto
    ): ResponseEntity<CustomerView> {
        val customer = this.customerService.findById(id)
        val customerUpdate = customerUpdateDto.toEntity(customer)
        val customerUpdated = this.customerService.save(customerUpdate)
        return ResponseEntity.status(HttpStatus.OK).body(CustomerView(customerUpdated))
    }

}