
CREATE TABLE USER( 
	UserID Int auto_increment NOT NULL,  
	FirstName VarChar(25) NOT NULL, 
	LastName VarChar(25) NOT NULL,  
	Age Int NOT NULL, 
	CurrentBill Numeric(9,2) NOT NULL DEFAULT '0.00',  
    	LateFees Numeric(9,2) NOT NULL DEFAULT '0.00',  
	EncryptedPassword VarChar(50) NOT NULL,  
	OutstandingBalance Boolean NOT NULL DEFAULT '0', 
	ReferredBy Integer NULL, 
	CONSTRAINT USER_PK PRIMARY KEY(UserID),  
	CONSTRAINT USER_ReferredBy_FK FOREIGN KEY(ReferredBy) 
		REFERENCES USER(UserID) 
			ON UPDATE CASCADE 
			ON DELETE NO ACTION 
); 

CREATE TABLE USER_CONTACT( 
	UserID Int NOT NULL,  
	EmailAddress VarChar(100) NOT NULL, 
	PhoneNumber VarChar(12) NOT NULL, 
	StreetAddress VarChar(35) NOT NULL, 
	City VarChar(35) NOT NULL, 
	State VarChar(2) NOT NULL, 
	Zipcode VarChar(5) NOT NULL, 
	CONSTRAINT USER_CONTACT_PK PRIMARY KEY(UserID),  
	CONSTRAINT USER_CONTACT_UserID_FK FOREIGN KEY(UserID) 
		REFERENCES USER(UserID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION 
); 

CREATE TABLE USER_MOVIE_PREFERENCES( 
	UserID Int NOT NULL,  
	MostPurchasedGenre VarChar(20) NULL DEFAULT 'NULL', 
	MostWatchedActor VarChar(50) NULL DEFAULT 'NULL', 
	MostWatchedDirector VarChar(50) NULL DEFAULT 'NULL', 
	CONSTRAINT USER_MOVIE_PREFERENCES_PK PRIMARY KEY(UserID),  
	CONSTRAINT USER_MOVIE_PREFERENCES_UserID_FK FOREIGN KEY(UserID) 
		REFERENCES USER(UserID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION 
);  

CREATE TABLE DIRECTOR( 
	DirectorID Int auto_increment NOT NULL, 
	FirstName VarChar(25) NOT NULL, 
	LastName VarChar(25) NOT NULL, 
	Biography VarChar(500) NULL DEFAULT 'NULL', 
	CONSTRAINT DIRECTOR_PK PRIMARY KEY(DirectorID) 
); 

CREATE TABLE PRODUCTION_COMPANY( 
	ProductionCompanyID Int auto_increment NOT NULL, 
	Headquarters VarChar(20) NULL DEFAULT 'NULL', 
	NumberOfEmployees Int NOT NULL DEFAULT '0', 
	CONSTRAINT PRODUCTION_COMPANY_PK PRIMARY KEY(ProductionCompanyID) 
); 

CREATE TABLE GENRE(
	GenreID Int auto_increment NOT NULL,
    	Genre VarChar(20) NOT NULL,
    	CONSTRAINT GENRE_PK PRIMARY KEY(GenreID)
);

CREATE TABLE MOVIE( 
	MovieID Int auto_increment NOT NULL, 
	Title VarChar(168) NOT NULL, 
	GenreID Int NOT NULL,  
	Description VarChar(500) NOT NULL, 
	MovieScore Numeric(4,2) NULL, 
	ReleaseDate Date NOT NULL, 
    	isDigital Boolean NOT NULL,
	NumberOfCopies Int NOT NULL DEFAULT '0', 
	RentalPrice Numeric(4,2) NOT NULL, 
    	MaximumRentalPeriodDays Int NOT NULL,
    	LateFeeRate Numeric(4,2) NOT NULL, 
	PurchasePrice Numeric(4,2) NOT NULL, 
	AgeRating VarChar(5) NOT NULL, 
	DirectorID Int NOT NULL, 
	SequelTo Int NULL, 
	ProductionCompanyID Int NULL, 
	CONSTRAINT MOVIE_PK PRIMARY KEY(MovieID), 
	CONSTRAINT MOVIE_DirectorID_FK FOREIGN KEY(DirectorID) 
		REFERENCES DIRECTOR(DirectorID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT MOVIE_SEQUELTO_FK FOREIGN KEY(SequelTo) 
		REFERENCES MOVIE(MovieID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT MOVIE_Production_Company_FK FOREIGN KEY(ProductionCompanyID) 
		REFERENCES PRODUCTION_COMPANY(ProductionCompanyID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT MOVIE_GENRE_FK FOREIGN KEY(GenreID)
		REFERENCES GENRE(GenreID)
			ON UPDATE NO ACTION
            		ON DELETE NO ACTION
); 

CREATE TABLE SKU_NUMBER(
	SKU Int auto_increment NOT NULL,
    	MovieID Int NOT NULL,
    	CONSTRAINT SKU_NUMBER_PK PRIMARY KEY(SKU),
    	CONSTRAINT SKU_NUMBER_MOVIE_FK FOREIGN KEY(MovieID)
		REFERENCES MOVIE(MovieID)
            ON DELETE CASCADE
);

CREATE TABLE REVIEW( 
	ReviewID Int auto_increment NOT NULL, 
	ReviewScore Int NOT NULL,  
	Text VarChar(500) NULL,  
	MovieID Int NOT NULL, 
	UserID Int NOT NULL, 
	CONSTRAINT REVIEW_PK PRIMARY KEY(ReviewID), 
	CONSTRAINT REVIEW_USER_FK FOREIGN KEY(UserID) 
		REFERENCES USER(UserID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT REVIEW_MOVIE_FK FOREIGN KEY(MovieID) 
		REFERENCES MOVIE(MovieID) 
			ON UPDATE NO ACTION 
			ON DELETE CASCADE, 
	CONSTRAINT REVIEW_ReviewScoreLimit  
		CHECK(ReviewScore BETWEEN 0 AND 10 OR ReviewScore = 'NULL') 
); 

CREATE TABLE EMPLOYEE( 
	EmployeeID Int auto_increment NOT NULL, 
	FirstName VarChar(25) NOT NULL, 
	LastName VarChar(25) NOT NULL, 
    	EncryptedPassword VarChar(50) NOT NULL,
	ManagedBy Int NULL, 
	CONSTRAINT EMPLOYEE_PK PRIMARY KEY(EmployeeID), 
	CONSTRAINT EMPLOYEE_ManagedBy_FK FOREIGN KEY(ManagedBy) 
		REFERENCES EMPLOYEE(EmployeeID) 
			ON UPDATE CASCADE 
			ON DELETE NO ACTION 
); 

CREATE TABLE TRANSACTION( 
	TransactionID Int auto_increment NOT NULL, 
    	ItemNumber Int NOT NULL,
	TransactionDate Date NOT NULL, 
	TransactionType VarChar(8) NOT NULL, 
	TotalPayment Numeric(9,2) NOT NULL, 
	EmployeeID Int NULL, 
	UserID Int NOT NULL, 
	CONSTRAINT TRANSACTION_PK PRIMARY KEY(TransactionID, ItemNumber), 
	CONSTRAINT TRANSACTION_EMPLOYEE_FK FOREIGN KEY(EmployeeID) 
		REFERENCES EMPLOYEE(EmployeeID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT TRANSACTION_USER_UserID_FK FOREIGN KEY(UserID) 
		REFERENCES USER(UserID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION, 
	CONSTRAINT TRANSACTION_TransactionType_AcceptedValues 
		CHECK(TransactionType IN('Rental', 'Purchase')) 
); 

CREATE TABLE RENTAL( 
	TransactionID Int NOT NULL,
    	ItemNumber Int NOT NULL,
	DueDate Date NOT NULL,
    	DateReturned Date NULL,
	IsPaid Boolean NOT NULL DEFAULT '0',  
	CONSTRAINT TRANSACTION_PK PRIMARY KEY(TransactionID, ItemNumber), 
	CONSTRAINT RENTAL_TRANSACTION_FK FOREIGN KEY(TransactionID, ItemNumber) 
		REFERENCES TRANSACTION(TransactionID, ItemNumber) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION 
); 

CREATE TABLE PURCHASE( 
	TransactionID Int NOT NULL, 
    	ItemNumber Int NOT NULL,
	PurchaseDate Date NOT NULL, 
    	HasWatched Boolean NOT NULL DEFAULT 0,
    	HasDownloaded Boolean NOT NULL DEFAULT 0,
	CONSTRAINT TRANSACTION_PK PRIMARY KEY(TransactionID, ItemNumber), 
	CONSTRAINT PURCHASE_TRANSACTION_FK FOREIGN KEY(TransactionID, ItemNumber) 
		REFERENCES TRANSACTION(TransactionID, ItemNumber) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION 
); 

CREATE TABLE MOVIE_TRANSACTION( 
	MovieID Int NOT NULL, 
	TransactionID Int NOT NULL, 
	CONSTRAINT MOVIE_TRANSACTION_PK PRIMARY KEY(MovieID,TransactionID), 
	CONSTRAINT MOVIE_TRANSACTION_MOVIE_FK FOREIGN KEY(MovieID) 
		REFERENCES MOVIE(MovieID)
			ON DELETE CASCADE, 
	CONSTRAINT MOVIE_TRANSACTION_TRANSACTION_FK FOREIGN KEY(TransactionID) 
		REFERENCES TRANSACTION(TransactionID) 
			ON UPDATE NO ACTION 
			ON DELETE NO ACTION 
);  

CREATE TABLE ACTOR( 
	ActorID Int auto_increment NOT NULL, 
	FirstName VarChar(25) NOT NULL, 
	LastName VarChar(25) NOT NULL, 
	Biography VarChar(500) NULL DEFAULT 'NULL', 
	CONSTRAINT ACTOR_PK PRIMARY KEY(ActorID) 
); 

CREATE TABLE MOVIE_ACTOR( 
	MovieID Int NOT NULL, 
	ActorID Int NOT NULL, 
	Role VarChar(50) NOT NULL, 
	CONSTRAINT MOVIE_ACTOR_PK PRIMARY KEY(MovieID, ActorID), 
	CONSTRAINT MOVIE_ACTOR_MOVIE_FK FOREIGN KEY(MovieID) 
		REFERENCES MOVIE(MovieID) 
			ON UPDATE NO ACTION 
			ON DELETE CASCADE, 
	CONSTRAINT MOVIE_ACTOR_ACTOR_FK FOREIGN KEY(ActorID) 
		REFERENCES ACTOR(ActorID) 
			ON UPDATE NO ACTION 
			ON DELETE CASCADE  
); 

CREATE TABLE MOVIE_MOVIE( 
	MovieID Int NOT NULL, 
	RelatedMovie Int NOT NULL, 
	CONSTRAINT MOVIE_MOVIE_PK PRIMARY KEY(MovieID, RelatedMovie), 
	CONSTRAINT MOVIE_MOVIE_MOVIE_FK FOREIGN KEY(MovieID) 
		REFERENCES Movie(MovieID) 
			ON UPDATE NO ACTION 
			ON DELETE CASCADE, 
	CONSTRAINT MOVIE_MOVIE_RELATEDMOVIE_FK FOREIGN KEY(RelatedMovie) 
		REFERENCES Movie(MovieID) 
			ON UPDATE NO ACTION 
			ON DELETE CASCADE   
); 

CREATE VIEW REVENUE_REPORT_TITLE_AND_GENRE AS(
	SELECT M.Title AS "Title", G.Genre AS "Genre", T.TotalPayment AS "TotalPayment", T.TransactionType AS "TransactionType"
    	FROM MOVIE AS M JOIN MOVIE_TRANSACTION AS MT
		ON M.MovieID = MT.MovieID JOIN TRANSACTION AS T 
			ON T.TransactionID = MT.TransactionID JOIN GENRE AS G
				ON G.GenreID = M.GenreID
	GROUP BY M.Title, G.Genre
);

CREATE VIEW REVENUE_REPORT_PERIODIC AS(
	SELECT WEEK(T.TransactionDate) AS "Week", MONTH(T.TransactionDate) AS "Month", YEAR(T.TransactionDate) AS "Year", SUM(T.TotalPayment) AS "TotalPayment", T.TransactionType AS "TransactionType"
    	FROM TRANSACTION AS T
    	GROUP BY WEEK(T.TransactionDate), MONTH(T.TransactionDate), YEAR(T.TransactionDate), T.TransactionType
);

CREATE VIEW USER_BALANCE_VIEW AS(
	SELECT UserID, LastName, FirstName, CurrentBill, LateFees
    	FROM USER
);

