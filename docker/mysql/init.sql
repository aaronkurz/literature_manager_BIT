-- Initial database and schema for literature_manager (simplified for local research tool)
CREATE DATABASE IF NOT EXISTS manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE manager;

-- article_info table (main articles) - removed userid field
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
  pathpdf VARCHAR(1024)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- article_summary table (simplified to one summary length)
CREATE TABLE IF NOT EXISTS article_summary (
  id INT AUTO_INCREMENT PRIMARY KEY,
  model VARCHAR(64),
  title VARCHAR(1024),
  fullSummary TEXT,
  summary1 TEXT, 
  summary2 TEXT, 
  summary3 TEXT, 
  summary4 TEXT, 
  summary5 TEXT, 
  summary6 TEXT,
  target TEXT,
  algorithm1 TEXT, 
  algorithm2 TEXT, 
  algorithm3 TEXT, 
  algorithm4 TEXT,
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

-- Example sample data (optional)
INSERT INTO article_info (title, author, summary, pubtime)
VALUES ('Sample Article', 'Test Author', 'This is a sample article for the local research tool.', '2025-01-01')
ON DUPLICATE KEY UPDATE title = title;
