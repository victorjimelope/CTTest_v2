package com.cttest.service.impl;

import com.cttest.model.entity.TestInstance;
import com.cttest.repository.TestInstanceRepository;
import com.cttest.service.TestInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TestInstanceServiceImpl implements TestInstanceService {

    private final TestInstanceRepository testInstanceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TestInstance> findAll() {
        return testInstanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestInstance> findAll(Pageable pageable) {
        return testInstanceRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TestInstance findById(Integer id) {
        return testInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TestInstance not found with id: " + id));
    }

    @Override
    @Transactional
    public TestInstance save(TestInstance testInstance) {
        return testInstanceRepository.save(testInstance);
    }

    @Override
    @Transactional
    public TestInstance update(TestInstance testInstance) {
        return testInstanceRepository.save(testInstance);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        testInstanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestInstance> findByTestId(Integer testId) {
        return testInstanceRepository.findByTestId(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestInstance> findByPin(Integer pin) {
        return testInstanceRepository.findByPin(pin);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getIdByPin(Integer pin) {
        return testInstanceRepository.getIdByPin(pin);
    }

    @Override
    @Transactional
    public Integer generatePin() {
        int pin;
        do {
            pin = 100000 + ThreadLocalRandom.current().nextInt(900000);
        } while (testInstanceRepository.findByPin(pin).isPresent());
        return pin;
    }

}
