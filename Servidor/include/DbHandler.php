<?php
/**
 *
 * @About:      Database connection manager class
 * @File:       Database.php
 * @Date:       $Date:$ Nov-2015
 * @Version:    $Rev:$ 1.0
 * @Developer:  Federico Guzman (federicoguzman@gmail.com)
 **/
class DbHandler {
 
    private $conn;
 
    function __construct() {
        require_once "DbConnect.php";
        // opening db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }
	
	public function getConn()
    {
        return $this->conn;
    }
 
    public function createAuto($array)
    {
        //aqui puede incluir la logica para insertar el nuevo auto a tu base de datos
    }
 
}
 
?>