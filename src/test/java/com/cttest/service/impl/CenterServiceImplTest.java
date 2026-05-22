package com.cttest.service.impl;

import com.cttest.model.entity.Center;
import com.cttest.repository.CenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterServiceImplTest {

    @Mock
    private CenterRepository centerRepository;

    private CenterServiceImpl centerService;

    private Center center;

    @BeforeEach
    void setUp() {
        centerService = new CenterServiceImpl(centerRepository);
        center = new Center();
        center.setId(1);
        center.setName("Test Center");
    }

    @Test
    void findAll_ShouldReturnAllCenters() {
        when(centerRepository.findAll()).thenReturn(List.of(center));

        List<Center> result = centerService.findAll();

        assertEquals(1, result.size());
        assertEquals("Test Center", result.get(0).getName());
        verify(centerRepository).findAll();
    }

    @Test
    void findById_WhenExists_ShouldReturnCenter() {
        when(centerRepository.findById(1)).thenReturn(Optional.of(center));

        Center result = centerService.findById(1);

        assertNotNull(result);
        assertEquals("Test Center", result.getName());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(centerRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> centerService.findById(99));
    }

    @Test
    void save_ShouldPersistCenter() {
        when(centerRepository.save(any(Center.class))).thenReturn(center);

        Center result = centerService.save(center);

        assertNotNull(result);
        assertEquals("Test Center", result.getName());
        verify(centerRepository).save(center);
    }

    @Test
    void deleteById_ShouldRemoveCenter() {
        doNothing().when(centerRepository).deleteById(1);

        centerService.deleteById(1);

        verify(centerRepository).deleteById(1);
    }

}
