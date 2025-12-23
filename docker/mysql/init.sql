-- Initial database and schema for literature_manager (simplified for local research tool)
CREATE DATABASE IF NOT EXISTS manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE manager;

-- article_info table (main articles) - removed userid field
CREATE TABLE IF NOT EXISTS article_info (
  id INT AUTO_INCREMENT PRIMARY KEY,
  srcdatabase VARCHAR(128),
  title VARCHAR(1024),
  author TEXT,
  organ TEXT,
  source VARCHAR(512),
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
  custom_concept1 TEXT,
  custom_concept2 TEXT,
  custom_concept3 TEXT
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

-- processing_status table (tracks paper processing workflow)
CREATE TABLE IF NOT EXISTS processing_status (
  id INT AUTO_INCREMENT PRIMARY KEY,
  task_id VARCHAR(64) UNIQUE NOT NULL,
  file_name VARCHAR(512),
  status VARCHAR(32) NOT NULL,
  progress INT DEFAULT 0,
  current_step VARCHAR(255),
  error_message TEXT,
  extracted_title VARCHAR(1024),
  extracted_authors TEXT,
  extracted_institution TEXT,
  extracted_year VARCHAR(16),
  extracted_source VARCHAR(512),
  extracted_keywords TEXT,
  extracted_doi VARCHAR(255),
  extracted_abstract TEXT,
  extracted_summary TEXT,
  extracted_custom_concept1 TEXT,
  extracted_custom_concept2 TEXT,
  extracted_custom_concept3 TEXT,
  file_path VARCHAR(1024),
  created_time DATETIME,
  updated_time DATETIME,
  completed_time DATETIME,
  INDEX idx_task_id (task_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- custom_concepts table (user-defined concepts for graph personalization)
CREATE TABLE IF NOT EXISTS custom_concepts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  relationship_name VARCHAR(128) NOT NULL,
  concepts TEXT NOT NULL,
  display_order INT NOT NULL,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
