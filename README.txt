********************************************************************************
This directory contains two entirely separate projects:
    - tradeintel_listingretriever
    - tradeintel_web
and a shared lib folder that contains our own libraries that are shared by both
projects.
********************************************************************************
To build:
    - Go into the sub-directory of the project you wish to run.
    - In a command window, type: mvn clean install
This has now built, tested & installed all the necessary files of this project
to your local maven repository.
********************************************************************************
To deploy without using NetBeans:
    - Start glassfish, then in the browser select Applications-->Deploy
    - Find the .ear file you installed to your local directory.
    - Click 'OK'
The application will now deploy and should be visible by visiting its context
root in your browser.
/tradeintel  for the web project.
/listingretriever  for the listingretreiver.
********************************************************************************

Import into Netbeans:
    - Simply 'Open Project' and navigate into the directory tradeintel.
    - Choose either 'tradeintel_listingretriever' or 'tradeintel_web'
    - All the applicable sub-projects will also be loaded into Netbeans.

********************************************************************************
To build or run project in Netbeans:
    - To build an entire project, right click on either 
      'tradeintel_listingretriever' or 'tradeintel_web' and choose 'Clean & 
      Build'. This will build all sub-projects.
    - To run the entire project, right click run on either 
      'tradeintel_listingretriever_ear' or 'tradeintel_web_ear' and click 'Run'.

********************************************************************************