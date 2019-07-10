<?php
/**
 *
 * @About:      Database connection manager class
 * @File:       Database.php
 * @Date:       $Date:$ Nov-2015
 * @Version:    $Rev:$ 1.0
 * @Developer:  Federico Guzman (federicoguzman@gmail.com)
 **/
class DbConnect {
 
    private $conn;
 
    function __construct() {        
    } 
    /**
     * Establishing database connection
     * @return database connection handler
     */
    function connect() {
        include_once "Config.php";

        try {
            
			$db_username = 'epiz_24048846';
			$db_password = 'InfinityPanel';
			$db_name = 'epiz_24048846_terrarium';
			$db_host = 'sql213.epizy.com';
			/*$this->conn = new PDO('mysql:host=' .
                            DB_HOST.';dbname='.
                            DB_NAME.';charset=utf8', 
                            DB_USERNAME, 
                            DB_PASSWORD);*/
			$this->conn = new PDO('mysql:host='.$db_host.';dbname='.$db_name.'', $db_username, $db_password);
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->conn->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);

            // returing connection resource
            return $this->conn;

        } catch(PDOException $ex) {
				
				
				  echo 'An error occured connecting to the database! Details: ' . $ex->getMessage();
            // if the environment is development, show error details, otherwise show generic message
           /* if ( (defined('ENVIRONMENT')) && (ENVIRONMENT == 'development') ) {
                echo 'An error occured connecting to the database! Details: ' . $ex->getMessage();
            } else {
                echo 'An error occured connecting to the database!'  . $ex->getMessage();
            }*/
            exit;
        }
        
    }
 
}
?>