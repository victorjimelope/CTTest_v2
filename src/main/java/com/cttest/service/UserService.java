package com.cttest.service;

import com.cttest.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService extends BaseService<User, Integer> {

	User save(User user, boolean encodePassword);

	/**
	 * Busca usuarios filtrando opcionalmente por centro.
	 *
	 * @param query texto a buscar en el nombre de usuario
	 * @param centerId identificador del centro, o {@code null} para no filtrar
	 * @param pageable paginación y orden
	 * @return página de usuarios que cumplen los filtros
	 */
	Page<User> searchByCenter(String query, Integer centerId, Pageable pageable);
}
