/*
    @Version 07.03.14
    @Author: Tim Grunshaw
    Part of the removing old data issue...
    Adding an auction multiple times resulted in a new row for questionpages
    everytime, with a new generated id. Also a new questionpages_question row. Bad.
    Because question has a real id (not generated), the question would reference
    multiple rows in questionpages_question. Upon deleting a question (when
    removing an auction), it would try to delete a question_pages row which 
    was not part of the latest added listeditemdetail. This failed due to foreign
    key constraints. Making it delete on cascade gets around this issue, but you
    still end up with orphaned questionpages (same with bid, bidpages, and 
    bidpages_bid.

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


ALTER TABLE `questionpages_question` 
DROP FOREIGN KEY `FK_QUESTIONPAGES_QUESTION_list_LISTINGQUESTIONID`;
ALTER TABLE `questionpages_question` 
ADD CONSTRAINT `FK_QUESTIONPAGES_QUESTION_list_LISTINGQUESTIONID`
  FOREIGN KEY (`list_LISTINGQUESTIONID`)
  REFERENCES `question` (`LISTINGQUESTIONID`)
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