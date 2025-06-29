/**
 * Muestra el modal dado su ID
 * 
 * @author victorjl 20250326
 */
function openModal(modalId) {
	new bootstrap.Modal(document.getElementById(modalId)).show();
}

/**
 * Oculta el modal dado su ID
 * 
 * @author victorjl 20250326
 */
function closeModal(modalId) {
	bootstrap.Modal.getInstance(document.getElementById(modalId)).hide();
}

/**
 * Oculta el dropdown dado su ID
 * 
 * @author victorjl 20250411
 */
function closeDropdownById(dropdownId) {
	
	const dropdown = document.getElementById(dropdownId);
	if (!dropdown) return;

	const toggleButton = dropdown.querySelector('[data-bs-toggle="dropdown"]');
	const dropdownInstance = bootstrap.Dropdown.getInstance(toggleButton);
	if (dropdownInstance) {
		dropdownInstance.hide();
	}
	
}

/**
 * Inicializa los tooltips // No funciona para a4j:commandLink
 * 
 * @author victorjl 20250510
 */
document.addEventListener("DOMContentLoaded", () => {
	const list = document.querySelectorAll('[data-bs-toggle="tooltip"]');
	list.forEach(item => {new bootstrap.Tooltip(item);});
});


