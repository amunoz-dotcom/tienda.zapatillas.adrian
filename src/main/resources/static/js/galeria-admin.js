(function () {
    "use strict";

    var entrada = document.getElementById("imagenes");
    var contenedor = document.getElementById("nuevas-imagenes-preview");
    if (!entrada || !contenedor) return;

    var archivos = [];
    var urls = [];
    var seleccionInicial = contenedor.dataset.currentCover || "";

    function actualizarEntrada() {
        var transferencia = new DataTransfer();
        archivos.forEach(function (archivo) {
            transferencia.items.add(archivo);
        });
        entrada.files = transferencia.files;
    }

    function mostrarPrevisualizaciones() {
        urls.forEach(URL.revokeObjectURL);
        urls = [];
        contenedor.replaceChildren();
        archivos.forEach(function (archivo, indice) {
            var url = URL.createObjectURL(archivo);
            urls.push(url);
            var tarjeta = document.createElement("article");
            tarjeta.className = "product-image-preview";
            var imagen = document.createElement("img");
            imagen.src = url;
            imagen.alt = archivo.name;
            var nombre = document.createElement("span");
            nombre.textContent = archivo.name;
            var etiqueta = document.createElement("label");
            var radio = document.createElement("input");
            radio.type = "radio";
            radio.name = "imagenPrincipal";
            radio.value = "new:" + indice;
            radio.checked = seleccionInicial === radio.value
                || (!seleccionInicial && contenedor.dataset.defaultCover === "true" && indice === 0);
            radio.addEventListener("change", function () {
                seleccionInicial = radio.value;
            });
            etiqueta.append(radio, document.createTextNode(" Imagen principal"));
            var quitar = document.createElement("button");
            quitar.type = "button";
            quitar.textContent = "Quitar";
            quitar.addEventListener("click", function () {
                archivos.splice(indice, 1);
                if (seleccionInicial === "new:" + indice) {
                    seleccionInicial = contenedor.dataset.currentCover || "";
                }
                actualizarEntrada();
                mostrarPrevisualizaciones();
            });
            tarjeta.append(imagen, nombre, etiqueta, quitar);
            contenedor.append(tarjeta);
        });
    }

    entrada.addEventListener("change", function () {
        archivos = Array.from(entrada.files || []);
        seleccionInicial = contenedor.dataset.currentCover || "";
        actualizarEntrada();
        mostrarPrevisualizaciones();
    });
})();
