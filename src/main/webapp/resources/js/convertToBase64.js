/**
 * Recibe el índice de la pregunta.
 * Encuentra el input de tipo "file" con el ID indicado.
 * Obtiene el fichero y lo lee en Base64.
 * Llama al método indicado y le pasa la información en
 * Base64 y el tipo de fichero como parámetros.
 * 
 * @author victorjl 20250320
 */
function convertToBase64(fileInputId, methodName, index) {
	
	const fileInput = document.getElementById(fileInputId);

    if (!fileInput || !fileInput.files.length) {
        return;
    }

    const file = fileInput.files[0];
    const mimeType = file.type;
	const reader = new FileReader();
	
    reader.onload = function () {
		
        const dataAsBase64 = mimeType === "image/jpeg" || mimeType === "image/png"
			? reader.result.split(',')[1] : "";
			
        if (typeof window[methodName] === "function") {
            window[methodName](index, dataAsBase64, mimeType);
        } else {
            console.error(`Error: method '${methodName}' is not defined.`);
        }
		
    };
	
	reader.readAsDataURL(file);
	
}
