<?php
ini_set('display_errors', 'On');
error_reporting(E_ALL);

/**
 *
 * @About:      API Interface
 * @File:       index.php
 * @Date:       $Date:$ Nov-2015
 * @Version:    $Rev:$ 1.0
 * @Developer:  Federico Guzman (federicoguzman@gmail.com)
 **/

/* Los headers permiten acceso desde otro dominio (CORS) a nuestro REST API o desde un cliente remoto via HTTP
 * Removiendo las lineas header() limitamos el acceso a nuestro RESTfull API a el mismo dominio
 * Nótese los métodos permitidos en Access-Control-Allow-Methods. Esto nos permite limitar los métodos de consulta a nuestro RESTfull API
 * Mas información: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
 **/
header("Access-Control-Allow-Origin: *");
header('Access-Control-Allow-Credentials: true');
header('Access-Control-Allow-Methods: PUT, GET, POST, DELETE, OPTIONS');
header("Access-Control-Allow-Headers: X-Requested-With");
header('Content-Type: text/html; charset=utf-8');
header('P3P: CP="IDC DSP COR CURa ADMa OUR IND PHY ONL COM STA"'); 

//include_once '../include/Config.php';

/* Puedes utilizar este file para conectar con base de datos incluido en este demo; 
 * si lo usas debes eliminar el include_once del file Config ya que le mismo está incluido en DBHandler 
 **/
require_once '../include/DbHandler.php'; 

require '../libs/Slim/Slim.php'; 
\Slim\Slim::registerAutoloader(); 
$app = new \Slim\Slim();






$app->get('/:nombre/clima', function ($nombre)
{
	$response = array();
    $db = new DbHandler();
	 $con = $db->getConn();
	
		$textoQuery = 'SELECT clima.probabilidad, clima.temperatura,clima.nombre FROM ubicacion INNER JOIN clima ON clima.idUbicacion = ubicacion.id WHERE ubicacion.nombre = :nombre ';
        
		$sth = $con->prepare($textoQuery);
 
        $sth->bindParam(':nombre', $nombre, PDO::PARAM_STR,255);
        $sth->execute();
 
        $clima = $sth->fetch(PDO::FETCH_OBJ);
		 
		if($clima == false)
			$response = array();
		else
			$response["clima"] = $clima;

    echoResponse(200, $response);

});

$app->get('/:nombre/estadistica', function ($nombre)
{
	$response = array();
    $db = new DbHandler();
	 $con = $db->getConn();
	
		$textoQuery = 'SELECT estadistica.temperaturaAnual, estadistica.precipitacionAnual
    FROM ubicacion INNER JOIN estadistica
    ON estadistica.idUbicacion = ubicacion.id
    WHERE ubicacion.nombre = :nombre ';
        $sth = $con->prepare($textoQuery);
 
        $sth->bindParam(':nombre', $nombre, PDO::PARAM_STR,255);
        $sth->execute();
 
        $estadistica = $sth->fetch(PDO::FETCH_OBJ);
		 if($estadistica == false)
			 $response =array();
		 else
			$response["estadistica"] = $estadistica;

    echoResponse(200, $response);

});

$app->get('/:nombre', function ($nombre)
 {
    
    $response = array();
    $db = new DbHandler();
	 $con = $db->getConn();
	
 
        $sth = $con->prepare("SELECT * 
            FROM tiposuelo
            WHERE nombre = :nombre");
 
        $sth->bindParam(':nombre', $nombre, PDO::PARAM_STR,255);
        $sth->execute();
 
        $suelo = $sth->fetch(PDO::FETCH_OBJ);
		if($suelo == false)
			$response = array();
		else	
			$response["suelo"] = $suelo;

    echoResponse(200, $response);
});

$app->get('/:tiposuelo/:tipoplanta', function ($nombre,$nombre_tipoplanta)
 {
    
    $response = array();
    $db = new DbHandler();
	$con = $db->getConn();
	
 
        $sth = $con->prepare("SELECT * 
            FROM tiposuelo
            WHERE nombre = :nombre and tipoPlanta = :tipoplanta");
 
        $sth->bindParam(':nombre', $nombre, PDO::PARAM_STR,255);
		$sth->bindParam(':tipoplanta', $nombre_tipoplanta, PDO::PARAM_STR,255);
        $sth->execute();
 
        $suelo = $sth->fetch(PDO::FETCH_OBJ);
		if($suelo == false)
			$response = array();
		else	
			$response["suelo"] = $suelo;

    echoResponse(200, $response);
});






/* corremos la aplicación */
$app->run();

/*********************** USEFULL FUNCTIONS **************************************/

/**
 * Verificando los parametros requeridos en el metodo o endpoint
 */
function verifyRequiredParams($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;
    // Handling PUT request params
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params);
    }
    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }
 
    if ($error) {
        // Required field(s) are missing or empty
        // echo error json and stop the app
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
        echoResponse(400, $response);
        
        $app->stop();
    }
}
 
/**
 * Validando parametro email si necesario; un Extra ;)
 */
function validateEmail($email) {
    $app = \Slim\Slim::getInstance();
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response["error"] = true;
        $response["message"] = 'Email address is not valid';
        echoResponse(400, $response);
        
        $app->stop();
    }
}
 
/**
 * Mostrando la respuesta en formato json al cliente o navegador
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoResponse($status_code, $response) {
    $app = \Slim\Slim::getInstance();
    // Http response code
    $app->status($status_code);
 
    // setting response content type to json
    $app->contentType('application/json');
 
    echo json_encode($response);
}

/**
 * Agregando un leyer intermedio e autenticación para uno o todos los metodos, usar segun necesidad
 * Revisa si la consulta contiene un Header "Authorization" para validar
 */
function authenticate(\Slim\Route $route) {
    // Getting request headers
    $headers = apache_request_headers();
    $response = array();
    $app = \Slim\Slim::getInstance();
 
    // Verifying Authorization Header
    if (isset($headers['Authorization'])) {
        //$db = new DbHandler(); //utilizar para manejar autenticacion contra base de datos
 
        // get the api key
        $token = $headers['Authorization'];
        
        // validating api key
        if (!($token == API_KEY)) { //API_KEY declarada en Config.php
            
            // api key is not present in users table
            $response["error"] = true;
            $response["message"] = "Acceso denegado. Token inválido";
            echoResponse(401, $response);
            
            $app->stop(); //Detenemos la ejecución del programa al no validar
            
        } else {
            //procede utilizar el recurso o metodo del llamado
        }
    } else {
        // api key is missing in header
        $response["error"] = true;
        $response["message"] = "Falta token de autorización";
        echoResponse(400, $response);
        
        $app->stop();
    }
}
?>