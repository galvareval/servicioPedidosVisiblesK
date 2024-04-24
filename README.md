Servicio escritorio para actulizar los datso de la BBDD de la aplicación de cocina, Se ejecuta en segundo plano al pasar 1 hora.
Notifica a los usuarios mediante una notificación push
cuando queden 30 minutos hasta que pueda seguir modificando el pedido.
cuando pasa la hora y ya no pueden modificar el pedido.
cuando un pedido pasa a estado recoger pudiendo el usuario pasar a recogerlo





Parte de WEB:
desde la fecha pedido modifica el estado editable del pedido, que al principio esta en edtiable:true a editable:false. De esta manera el pedido ya no se mostrará como editable en la aplicación y se cumple la restricción de tiempo para modificar un pedido establecida por el departamento de cocina.
