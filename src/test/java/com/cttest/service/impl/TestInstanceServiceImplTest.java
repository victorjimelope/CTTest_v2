package com.cttest.service.impl;

import com.cttest.model.entity.TestInstance;
import com.cttest.repository.TestInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestInstanceServiceImplTest {

    @Mock
    private TestInstanceRepository testInstanceRepository;

    private TestInstanceServiceImpl testInstanceService;

    @BeforeEach
    void setUp() {
        testInstanceService = new TestInstanceServiceImpl(testInstanceRepository);
    }

    @Test
    void getIdByPin_WhenPinExists_ShouldReturnId() {
        when(testInstanceRepository.getIdByPin(123456)).thenReturn(Optional.of(1));

        Optional<Integer> result = testInstanceService.getIdByPin(123456);

        assertTrue(result.isPresent());
        assertEquals(1, result.get());
    }

    @Test
    void getIdByPin_WhenPinNotExists_ShouldReturnEmpty() {
        when(testInstanceRepository.getIdByPin(999999)).thenReturn(Optional.empty());

        Optional<Integer> result = testInstanceService.getIdByPin(999999);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByPin_ShouldReturnTestInstance() {
        TestInstance instance = new TestInstance();
        instance.setId(1);
        instance.setPin(123456);
        when(testInstanceRepository.findByPin(123456)).thenReturn(Optional.of(instance));

        Optional<TestInstance> result = testInstanceService.findByPin(123456);

        assertTrue(result.isPresent());
        assertEquals(123456, result.get().getPin());
    }

    @Test
    void generatePin_ShouldReturn6DigitPin() {
        when(testInstanceRepository.findByPin(anyInt())).thenReturn(Optional.empty());

        Integer pin = testInstanceService.generatePin();

        assertNotNull(pin);
        assertTrue(pin >= 100000 && pin <= 999999);
    }

}
