### Terrarium
Terrario IoT para la materia de Sistemas Operativos Avanzados - Unlam

### Descripcion
Por medio de una placa arduino proponemos crear un terrario automatizado con riego automático para distintos tipos de plantas. Con el uso de sensores vamos a tomar mediciones de las condiciones del terreno, para que posteriormente actúen los actuadores en caso de ser necesario.
Las mismas puedan ser visualizadas en una app mobile y en base a eso poder tomar alguna decisión.

### Sensores
* Sensor Temperatura
* Sensor Humedad de la tierra
* Sensor Lluvia por goteo

### Actuadores
* Cooler para ventilación
* Bomba de agua para riego
* Servo motores para el techo corredizo
* Display
* Led's 

### Otros
* Modulo Bluetooth para la comunicación entre la app y arduino

### Funcionamiento 

Se dispondrán de 2 modos de uso: modo inteligente y modo manual.

* Modo inteligente

En el modo inteligente el usuario deberá ingresar la ubicación donde se intalara el terrario mediante la app móvil,
consultamos los servicios en la nube para determinar el tipo de tierra, en base a las temperaturas y lluvias anuales.
Ya sabiendo el tipo de tierra, se solicitará al usuario que seleccione de una lista el/los tipo de plantas que va a colocar.
Con esta información nosotros vamos a determinar la humedad, temperatura y riego ideales para el mantenimiento de la misma.
Además para los días que este pronosticado lluvia, te vamos a avisar a través de la aplicación para poder aprovechar la lluvia para el riego de las plantas mediante un techo corredizo.

* Modo manual

En el modo manual vamos a dar la posibilidad al usuario mediante la aplicación móvil activar o desactivar los distintos actuadores.

