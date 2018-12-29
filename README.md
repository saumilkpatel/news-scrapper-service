# News Scrapper Service

This project consists of two parts :
1) Scrapping news data from data archive of **The Hindu** and store it in **SQLite** database.
2) Build REST APIs for querying in the database using **Dropwizard framework**.


## Config

There are two config files in the project.


 1. **rest-config.yml**

		 # turning this true will scrap the data on server startup based on scrapper-config.yml
		 scrappingEnabledOnStartup: false


 2. **scrapper-config.yml**

		# articles will be scrapped from below path
		baseUrl: 'https://www.thehindu.com/archive/print'
		dateOfDataToScrap: '2018/12/26'

		# below is the denoted path where the database will be stored
		dbOutPath: 'jdbc:sqlite:/home/saumilpatel/Desktop/handzap/%s.db'
		dbName: 'NewsScrapperService'

		# turning this true will recreate the database else will append in current one
		recreateDB: false

		# will store/append the data of below no of articles
		maxNoOfArticlesToScrap: 50


## Running Application

1)  **ScrapperApplication.java**
	Change the **scrapper-config.yml** appropriately then run this class. This will scrap the data for a particular date (configurable) limited by the number of articles (configurable) from the URL provided.

2) **RestApplication.java**
Change the **rest-config.yml** appropriately then run this class. Running this class will start a server. Once started, following APIs can be used for querying.

		1) Search available Authors :

			GET /news-service/authors HTTP/1.1
			Host: localhost:8080
			cache-control: no-cache
			Postman-Token: 1595ec92-fb47-4a33-9856-d6bc7ebdacb1

		2) Search articles based on author name :

			GET /news-service/articles-by-author?authorName=special correspondent&amp; limit=10 HTTP/1.1
			Host: localhost:8080
			cache-control: no-cache
			Postman-Token: 9fe0ce20-cbd5-45a0-b55e-d4de4d1bafac

		3) Search articles based on title and description :

			GET /news-service/articles-by-content?title=someTitle&amp; description=someDescription&amp; limit=5 HTTP/1.1
			Host: localhost:8080
			cache-control: no-cache
			Postman-Token: 9b86a5aa-2f9c-4247-a349-1872fc154bb8

		4) Scrap and store the data into database

			PUT /scrapper-service/scrap-data?recreateDB=true&amp; limit=10 HTTP/1.1
			Host: localhost:8080
			Content-Type: application/x-www-form-urlencoded
			cache-control: no-cache
			Postman-Token: 32cd6388-2fa2-45cd-bd65-2df11835fa39





## Important Notes

**Scrapping data can be done three ways:**

1) By running ScrapperApplication.java
2) On server startup (By running RestApplication.java) with **scrappingEnabledOnStartup: true** in rest-config.yml
3) After server starts, By hitting API-4 from listed above.

**SQLite database is used for data storage and retrieval**
**Dropwizard framework is used to build and integrate REST APIs.**
**Jsoup framework is used for web-scrapping.**
**Maven architecture is used as Dependency Management Tool.**

**Postman Collection URL for integrted APIs**
	https://www.getpostman.com/collections/4e6e3cb2cf8cb22ed621
