package com.zbank.customerservice.support;

import com.zbank.customerservice.dto.CardActivationRequest;
import com.zbank.customerservice.entity.CreditCard;
import com.zbank.customerservice.entity.Customer;
import com.zbank.customerservice.entity.PinStatus;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static CardActivationRequest validActivationRequest() {
        return new CardActivationRequest(
                "1234567812345678",
                "1234",
                "DOC-123",
                "5678",
                "5678"
        );
    }

    public static Customer customer(Long id, String firstName, String lastName, String documentId) {
        Customer customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", id);
        ReflectionTestUtils.setField(customer, "firstName", firstName);
        ReflectionTestUtils.setField(customer, "lastName", lastName);
        ReflectionTestUtils.setField(customer, "documentId", documentId);
        return customer;
    }

    public static CreditCard pendingCard(Customer customer, String hashedPin) {
        CreditCard creditCard = new CreditCard();
        ReflectionTestUtils.setField(creditCard, "id", 100L);
        ReflectionTestUtils.setField(creditCard, "cardNumber", "1234567812345678");
        ReflectionTestUtils.setField(creditCard, "customer", customer);
        ReflectionTestUtils.setField(creditCard, "pinHash", hashedPin);
        ReflectionTestUtils.setField(creditCard, "pinStatus", PinStatus.PENDING_ACTIVATION);
        return creditCard;
    }
}
