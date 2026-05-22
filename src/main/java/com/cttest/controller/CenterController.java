package com.cttest.controller;

import com.cttest.model.entity.Center;
import com.cttest.model.dto.CenterDTO;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.service.CenterService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/centers")
public class CenterController extends BaseController<Center, CenterDTO, CenterService> {

    public CenterController(CenterService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return listAll(search, pageable);
    }

    @Override
    protected CenterDTO toDto(Center entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "centers";
    }
}
