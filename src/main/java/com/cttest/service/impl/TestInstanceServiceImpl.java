package com.cttest.service.impl;

import com.cttest.model.entity.TestInstance;
import com.cttest.repository.TestInstanceRepository;
import com.cttest.service.TestInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TestInstanceServiceImpl extends BaseServiceImpl<TestInstance, Integer, TestInstanceRepository> implements TestInstanceService {

    public TestInstanceServiceImpl(TestInstanceRepository repository) {
        super(repository, "TestInstance");
    }

    @Override
    @Transactional
    public TestInstance save(TestInstance testInstance) {
        if (testInstance.getId() == null) {
            testInstance.setAddDate(LocalDateTime.now());
        } else {
            repository.findById(testInstance.getId()).ifPresent(existing -> {
                if (testInstance.getAddDate() == null) {
                    testInstance.setAddDate(existing.getAddDate());
                }
                if (testInstance.getAddUser() == null) {
                    testInstance.setAddUser(existing.getAddUser());
                }
            });
        }
        if (testInstance.getAddUser() == null && testInstance.getAssignedBy() != null) {
            testInstance.setAddUser(testInstance.getAssignedBy().getId());
        }
        testInstance.setModDate(LocalDateTime.now());
        if (testInstance.getPin() == null) {
            testInstance.setPin(generatePin());
        }
        return repository.save(testInstance);
    }

    @Override
    @Transactional
    public TestInstance update(TestInstance testInstance) {
        return save(testInstance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestInstance> findByTestId(Integer testId) {
        return repository.findByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestInstance> findByPin(Integer pin) {
        return repository.findByPin(pin);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getIdByPin(Integer pin) {
        return repository.getIdByPin(pin);
    }

    @Override
    @Transactional
    public Integer generatePin() {
        int pin;
        do {
            pin = 100000 + ThreadLocalRandom.current().nextInt(900000);
        } while (repository.findByPin(pin).isPresent());
        return pin;
    }

}
