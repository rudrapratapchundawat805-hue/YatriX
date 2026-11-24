# 🚌 YatriX – Ahmedabad Multimodal Public Transportation Route Optimisation System

**Making urban commuting easier, smarter and more accessible.**

YatriX is a Java-based multimodal public transportation management and route recommendation system designed specifically for Ahmedabad, Gujarat.

The system integrates three major public transportation networks – AMTS, BRTS and Ahmedabad Metro – to help commuters discover suitable routes between two locations.

Instead of manually navigating different transportation networks, users can enter their starting point and destination and receive multiple journey alternatives, including estimated travel time, fare breakdowns, intermediate stations and transportation transfers.

---

## 📌 Problem Statement

Navigating an unfamiliar city can be challenging, especially when multiple public transportation systems operate independently.

For someone new to Ahmedabad, travelling from one location to another may require combining AMTS buses, BRTS buses and Metro services.

Identifying the most affordable or time-efficient combination of transportation options can be confusing and time-consuming.

YatriX aims to simplify this process by integrating these transportation networks into a unified route recommendation system.

## 💡 Proposed Solution

YatriX analyses the available transportation network and generates possible journeys between the user's source and destination.

The system considers:
- Available transportation routes.
- Intermediate stations.
- Estimated travel duration.
- Transportation changes and transfers.
- Passenger-specific fare calculations.

It presents multiple route alternatives, allowing commuters to compare journey costs and estimated travel times.

---

## ✨ Key Features

### 1. Multimodal Route Planning
- Integrates AMTS, BRTS and Metro networks.
- Supports journeys involving multiple transportation modes.
- Identifies transfer points between different services.
- Displays individual journey segments.

### 2. Route Optimisation
- Uses graph-based route searching.
- Finds time-efficient direct routes.
- Generates multiple multimodal journey alternatives.
- Ranks final journey recommendations by fare, travel time and number of transfers.

### 3. Passenger-Based Fare Calculation
- Separate fare calculation logic for AMTS, BRTS and Metro.
- Considers passenger information.
- Supports Metro smart-card discounts.
- Displays detailed fare breakdowns.

### 4. Journey Information
Each recommendation provides:
- Complete station sequence.
- Number of stops.
- Estimated travel duration.
- Transportation mode and route number.
- Transfer locations.
- Estimated arrival time.
- Standard and passenger-specific fares.

### 5. Search History
- Records previous route searches.
- Stores timestamps and journey information.
- Maintains a local search history file.

### 6. Exception Handling
- Custom exceptions for missing stations.
- Custom exceptions for unavailable routes.
- Input validation and error handling.

---

## 🛠️ Technology Stack

| Technology | Application |
|---|---|
| Java | Core application development |
| MySQL | Transportation database |
| JDBC | Java–MySQL connectivity |
| Java Collections Framework | Graph and route data management |
| PriorityQueue | Route searching and prioritisation |
| Multithreading | Parallel route searching |
| Excel | Transportation dataset preparation |

---

## 🧠 Technical Implementation

### Graph-Based Transportation Network

YatriX represents the transportation network using an adjacency-list graph.

Each station is represented by an identifier, while connections between stations contain information such as:

- Transportation mode.
- Route number.
- Destination station.
- Estimated travel time.
- Service frequency information.

The graph is constructed using information retrieved from the MySQL database.

### Route-Finding Algorithm

The application uses priority-queue-based shortest-path searching.

**Direct Route Searching:**
- Searches AMTS, BRTS and Metro independently.
- Uses cumulative travel time as the path priority.
- Finds the minimum-time route for each individual mode.

**Multimodal Route Searching:**
- Explores connections across different transportation modes.
- Tracks the current station and transportation mode.
- Identifies mode changes and transfer counts.
- Restricts journeys to a maximum of three transfers.
- Generates up to five multimodal route candidates.

### Route Ranking

The final recommendations are compared using three criteria:

1. Lowest passenger-specific fare.
2. Shortest estimated travel time.
3. Lowest number of transportation transfers.

This allows users to compare different journey alternatives.

### Multithreading

YatriX performs direct and multimodal route searches using separate threads.

This allows both searching processes to execute concurrently before their results are combined and displayed.

---

## 💰 Fare Calculation

YatriX implements separate fare calculators for each transportation system.

| Transport | Fare Calculation |
|---|---|
| AMTS | Station-based slab calculation with passenger-specific rules |
| BRTS | Station-based slab calculation |
| Metro | Station-based slab calculation with smart-card discount |

The implementation also includes passenger-specific fare conditions.

These are the fare rules implemented in the project and should not be interpreted as verified, current official transport tariffs.

---

## 🗄️ Database Architecture

YatriX uses MySQL to store and retrieve transportation information.

The Java application interacts with three primary database tables:

| Table | Purpose |
|---|---|
| `stations` | Stores station identifiers and station names |
| `routes` | Stores transportation modes and route information |
| `route_stops` | Stores station sequences and timing information for routes |

JDBC is used to retrieve records and construct the transportation graph dynamically when the application starts.

The project also includes an Ahmedabad transportation dataset in Excel format.

---

## 📂 Project Structure

```text
YatriX/
│
├── src/
│   └── com/
│       └── transit/
│           ├── Connection.java
│           ├── DatabaseConnector.java
│           ├── FareCalculator.java
│           ├── HistoryLogger.java
│           ├── PathNode.java
│           ├── RouteFinder.java
│           ├── TransitApp.java
│           ├── TransitException.java
│           ├── TransitResult.java
│           └── UserSession.java
│
├── database/
│   └── Ahmedabad_Transit_Data.xlsx
│
├── README.md
└── .gitignore
```

---

## ⚙️ Installation and Execution

### Prerequisites

- Java Development Kit (JDK)
- MySQL Server
- MySQL Connector/J
- IntelliJ IDEA or another Java IDE

### Setup Instructions

**1. Clone the repository**

```bash
git clone https://github.com/rudrapratapchundawat805-hue/YatriX.git
```

**2. Open the project**

Open the cloned project in IntelliJ IDEA.

**3. Configure MySQL**

Create a MySQL database named:

```sql
CREATE DATABASE ahmedabad_transit;
```

Prepare the required tables:

- `stations`
- `routes`
- `route_stops`

Import and organise the transportation dataset into the corresponding database tables.

**Note:** The Excel dataset is provided as source data. It is not an automatically importable MySQL database dump.

**4. Configure database connectivity**

Open `DatabaseConnector.java` and configure the JDBC URL, MySQL username and password for your local MySQL installation.

Ensure MySQL Connector/J is added to the project dependencies.

**5. Run the application**

Execute:

```text
TransitApp.java
```

Follow the terminal prompts to enter passenger details, starting location and destination.

---

## 🚀 Future Improvements

Potential improvements for future versions include:

- Graphical user interface.
- Interactive city map integration.
- Real-time public transportation updates.
- Live vehicle tracking.
- More advanced fare and travel-time optimisation.
- Mobile application integration.
- Automated database import and updates.

---

## 🎯 Project Objective

YatriX was developed to explore how data structures, graph algorithms, database management and object-oriented programming can be combined to address a practical urban transportation problem.

The project focuses on making public transportation navigation more accessible for commuters unfamiliar with Ahmedabad's transportation network.

---

**Developed as an academic project using Java and MySQL.**

*YatriX – Your city, your journey, simplified.*
