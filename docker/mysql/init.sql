-- Initial database and schema for literature_manager
CREATE DATABASE IF NOT EXISTS manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE manager;

-- role table (users)
CREATE TABLE IF NOT EXISTS role (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128),
  name VARCHAR(128),
  phone VARCHAR(20),
  email VARCHAR(128),
  avatar VARCHAR(255),
  role VARCHAR(16)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- notice table
CREATE TABLE IF NOT EXISTS notice (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  content TEXT,
  time VARCHAR(50),
  user VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- article_info table (main articles)
CREATE TABLE IF NOT EXISTS article_info (
  id INT AUTO_INCREMENT PRIMARY KEY,
  srcdatabase VARCHAR(128),
  title VARCHAR(1024),
  author TEXT,
  organ VARCHAR(255),
  source VARCHAR(255),
  keyword TEXT,
  summary TEXT,
  pubtime VARCHAR(64),
  firstduty VARCHAR(255),
  fund VARCHAR(255),
  year VARCHAR(16),
  pagecount VARCHAR(32),
  clc VARCHAR(128),
  url VARCHAR(1024),
  doi VARCHAR(255),
  patha VARCHAR(1024),
  pathb VARCHAR(1024),
  pathdocx VARCHAR(1024),
  pathtxt VARCHAR(1024),
  pathpdf VARCHAR(1024),
  userid VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- article_summary table
CREATE TABLE IF NOT EXISTS article_summary (
  id INT AUTO_INCREMENT PRIMARY KEY,
  model VARCHAR(64),
  title VARCHAR(1024),
  summary TEXT,
  short1 TEXT, short2 TEXT, short3 TEXT, short4 TEXT, short5 TEXT, short6 TEXT,
  mid1 TEXT, mid2 TEXT, mid3 TEXT, mid4 TEXT, mid5 TEXT, mid6 TEXT,
  long1 TEXT, long2 TEXT, long3 TEXT, long4 TEXT, long5 TEXT, long6 TEXT,
  target TEXT,
  algmid1 TEXT, algmid2 TEXT, algmid3 TEXT, algmid4 TEXT,
  alglong1 TEXT, alglong2 TEXT, alglong3 TEXT, alglong4 TEXT,
  environment TEXT,
  tools TEXT,
  datas TEXT,
  standard TEXT,
  result TEXT,
  future TEXT,
  weekpoint TEXT,
  keyword TEXT,
  ifteacher INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert a default admin user (password is plaintext as in original project)
INSERT INTO role (username, password, name, role)
VALUES ('admin', '123456', 'Administrator', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;

-- Example sample data (optional)
INSERT INTO article_info (title, author, summary, pubtime)
VALUES ('Sample Article', 'Test Author', 'This is a sample article created by init script.', '2025-01-01')
ON DUPLICATE KEY UPDATE title = title;
