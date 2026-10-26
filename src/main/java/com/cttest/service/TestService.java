package com.cttest.service;

import com.cttest.model.entity.Test;
import com.cttest.model.enums.TestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestService extends BaseService<Test, Integer> {

	Test findWithQuestionsById(Integer id);

	List<Test> findByCenterId(Integer centerId);

	/**
	 * Busca tests aplicando los filtros indicados.
	 *
	 * @param query texto a buscar en el nombre del test
	 * @param centerId identificador del centro, o {@code null} para no filtrar
	 * @param status estado del test, o {@code null} para no filtrar
	 * @param pageable paginación y orden
	 * @return página de tests que cumplen los filtros
	 */
	Page<Test> search(String query, Integer centerId, TestStatus status, Pageable pageable);
}
