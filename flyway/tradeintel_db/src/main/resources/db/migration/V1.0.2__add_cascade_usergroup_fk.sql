/*
    @Version 31.10.13
    @Author: Tim Grunshaw
    This adds a MySQL constraint (below EclipsLink leve) to cascade updates & 
    deletes.
    This is a particularly complicated field. It is used by security (JDBCRealm),
    which specifies a certain table structure (a column has to be the credential, 
    i.e. the 'email'). This means that it has a foreign key index on 'users.email', 
    specified in JPA (User.java). However, when changing a users email, we need 
    to update the foreign key (otherwise we get a foreign key constraint error).

    See Wiki & http://stackoverflow.com/questions/19471768/how-to-cascade-elementcollection-in-jpa-eclipselink

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


ALTER TABLE `users_groups` DROP FOREIGN KEY `FK_users_groups_email` ;
ALTER TABLE `users_groups` 
  ADD CONSTRAINT `FK_users_groups_email`
  FOREIGN KEY (`email` )
  REFERENCES `users` (`EMAIL` )
  ON DELETE CASCADE
  ON UPDATE CASCADE;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;