package com.decerto.leszek.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
public class CustomerTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testRetrieve() {

        for (Customer customer: customerRepository.findAll()) {
            System.out.println(customer.getName() + " " + customer.getSurname());
            for (Address address : customer.getAddresses()) {
                System.out.println(address.getStreet() + " " + address.getHouseNumber());
            }
        }

    }

}
