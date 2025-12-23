# Custom Concept-Based Graph Relationships Feature

## Overview
This feature allows users to define their own custom concepts that may connect papers, enabling personalized graph relationships beyond the default metadata-based connections.

## Key Features

### 1. Graph Personalization Page
- Users can define up to 3 custom concept combinations
- Each combination consists of:
  - **Relationship Name**: A semantically meaningful name (e.g., "method", "dataset", "evaluation_metric")
  - **Concepts**: Up to 5 potential concept values (e.g., "RCT", "Retrospective_Cohort", "Prospective_Cohort")

### 2. AI-Powered Concept Extraction
- When uploading papers, the LLM automatically identifies which custom concepts apply
- The extraction happens alongside regular metadata extraction
- Results are shown in the approval page for user review

### 3. Graph Visualization
- Custom concept relationships appear in the Neo4j graph
- Users can filter papers by custom concepts
- Custom concept nodes are displayed with a distinctive green color

## Usage Workflow

### Step 1: Define Custom Concepts
1. Navigate to "图谱个性化" (Graph Personalization) in the navigation menu
2. For each of the 3 available slots:
   - Enter a relationship name (e.g., "method")
   - Add up to 5 concept values (e.g., "RCT", "Cohort Study", "Case Study")
   - Click "保存概念组合" (Save Concept Combination)

**Example for Medical Research:**
- Relationship: `method`
- Concepts: `RCT`, `Retrospective_Cohort`, `Prospective_Cohort`, `Mendelian_Randomization_Study`, `Meta_Analysis`

### Step 2: Upload Papers
1. Upload a paper PDF as usual
2. The system will automatically:
   - Extract standard metadata (title, authors, abstract, etc.)
   - Check if the paper uses any of your defined custom concepts
   - Present results for approval

### Step 3: Review Extraction Results
- In the processing status page, you'll see:
  - Standard metadata fields (editable)
  - Custom concepts section (if any matches found)
  - Green tags showing which concepts were identified
- Approve or reject as usual

### Step 4: Visualize in Graph
1. Navigate to the knowledge graph page
2. Use the new "自定义概念查询" (Custom Concepts Query) section
3. Click on any custom concept to filter the graph
4. Custom concept nodes appear in green

## Technical Implementation

### Backend Components
- **Entity**: `CustomConcept` - Stores user-defined concepts
- **Controller**: `CustomConceptController` - REST API endpoints
- **Service**: `CustomConceptService` - Business logic with validation
- **Mapper**: `CustomConceptMapper` - Database access

### Database Schema
```sql
CREATE TABLE custom_concepts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  relationship_name VARCHAR(128) NOT NULL,
  concepts TEXT NOT NULL,  -- Semicolon-separated
  display_order INT NOT NULL,  -- 1, 2, or 3
  created_time DATETIME,
  updated_time DATETIME
);

ALTER TABLE processing_status 
  ADD COLUMN extracted_custom_concept1 TEXT,
  ADD COLUMN extracted_custom_concept2 TEXT,
  ADD COLUMN extracted_custom_concept3 TEXT;

ALTER TABLE article_info
  ADD COLUMN custom_concept1 TEXT,
  ADD COLUMN custom_concept2 TEXT,
  ADD COLUMN custom_concept3 TEXT;
```

### LLM Integration
The system constructs specialized prompts for each custom concept:
```
关系类型: method
可能的概念: RCT, Retrospective_Cohort, Prospective_Cohort

返回匹配的概念（JSON格式）:
{"concepts": ["RCT", "Cohort Study"]}
```

### Neo4j Graph Structure
Custom concepts are stored as:
- Node type: `自定义概念` (Custom Concept)
- Relationship: `自定义概念关系` (Custom Concept Relationship)
- Properties include both relationship name and concept value

## API Endpoints

### Custom Concept Management
- `GET /custom-concepts/list` - Get all custom concepts
- `GET /custom-concepts/{displayOrder}` - Get by display order
- `POST /custom-concepts/save` - Save or update concept
- `DELETE /custom-concepts/{displayOrder}` - Delete concept

## Performance Optimizations
1. **Caching**: Custom concepts are loaded once on page load
2. **Validation**: Input limits enforced at service layer (max 3 combinations, max 5 concepts each)
3. **Batching**: Custom concept extraction happens in parallel with metadata extraction
4. **Neo4j Indexing**: Relationships indexed for fast querying

## Constraints and Limitations
- Maximum 3 custom concept combinations per system
- Maximum 5 concepts per combination
- Concept names should use English or alphanumeric characters
- Custom concept extraction requires the paper content to explicitly mention the concepts

## Future Enhancements
- Allow users to edit extracted custom concepts before approval
- Support dynamic relationship types in Neo4j (currently uses generic relationship with type property)
- Add confidence scores for concept extraction
- Support hierarchical concepts (e.g., parent-child relationships)

## Troubleshooting

### Custom concepts not appearing in graph
- Ensure concepts were saved successfully
- Verify paper was processed after concepts were defined
- Check Neo4j database: `MATCH (n:自定义概念) RETURN n`

### LLM not extracting concepts correctly
- Ensure concept names are clear and unambiguous
- Paper content must explicitly mention the concepts
- Check Ollama service is running and model is loaded

### Database migration issues
- Run the SQL migration script manually if needed
- Restart the Spring Boot application
- Clear any cached data
