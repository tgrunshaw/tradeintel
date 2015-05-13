/*
    @Version 23.11.13
    @Author: Tim Grunshaw
    After a MySQL data truncation error we realised the sizes of the columns
    for prices were not big enough. They were Decimal(10,2) which allows
    10 significant values, 2 of which are after the decimal point. So in effect
    up to: $99,999,999.99. Trademe allows up to $999,999,999.99 at the moment,
    I've added another point to account for future inflation...

*/

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


ALTER TABLE `listeditemdetail` 
CHANGE COLUMN `BUYNOWPRICE` `BUYNOWPRICE` DECIMAL(12,2) NULL DEFAULT NULL  , 
CHANGE COLUMN `MAXBIDAMOUNT` `MAXBIDAMOUNT` DECIMAL(12,2) NULL DEFAULT NULL  , 
CHANGE COLUMN `MINIMUMNEXTBIDAMOUNT` `MINIMUMNEXTBIDAMOUNT` DECIMAL(12,2) NULL DEFAULT NULL  , 
CHANGE COLUMN `RESERVEPRICE` `RESERVEPRICE` DECIMAL(12,2) NULL DEFAULT NULL  , 
CHANGE COLUMN `STARTPRICE` `STARTPRICE` DECIMAL(12,2) NULL DEFAULT NULL  ;




/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;