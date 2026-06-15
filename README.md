## Demo Video
[
](https://youtu.be/3MKHKnQtoD4)# Thrive 🌱
### *spend smart. save more. grow further.*

Thrive is a personal finance tracker Android app built for South African users who are beginning their journey to financial independence.

---

## 👤 Student Details
- **Name:** Sinako Gulwa
- **Student Number:** ST10449972
- **Module:** OPSC6311 – Open Source Coding

---

## 📱 App Purpose
Thrive helps South African users track daily expenses, set budget goals, monitor savings, and stay motivated through a gamification system. All data is stored locally using SQLite — no internet connection required.

---

## ✨ Features

### Core Features
- **Register & Login** — secure local authentication with hashed passwords
- **Add Expenses** — log amount, category, description and date
- **Expense List** — view all expenses filtered by period
- **Budget Goals** — set monthly budget with per-category limits and colour-coded status
- **Savings Goals** — set savings targets with progress tracking
- **Badges & Rewards** — earn badges for streaks, budget goals and milestones

### Part 3 New Features
- **Spending Reports** — bar chart showing amount spent per category over a user-selectable period (This Month, Last Month, Last 3 Months) with min/max goal lines
- **Visual Goal Status** — colour-coded progress showing how well user stays within min/max spending goals

### Custom Features
1. **Spending Insights** — pie chart breakdown of spending by category with smart budget tips and a visual progress indicator showing budget usage percentage
2. **7-Day Streak Tracker** — tracks consecutive days of expense logging and awards streak badges (3-day, 7-day) with points system and level progression (Beginner → Saver → Planner → Thriver → Champion)

---

## 🎨 Design Considerations
- **Colour Scheme:** Deep Purple (#6C3FC5) primary, Lavender (#EDE7F6) secondary
- **Typography:** Poppins (headings), Nunito (body)
- **Category Colours:** Each category has a unique colour matching real-world brand associations (orange for food, black for transport, red for entertainment)
- **Offline First:** All data stored in local SQLite database — no internet required
- **Target Users:** South African young adults aged 18-30 beginning financial independence

---

## 🔧 GitHub Actions
This project uses GitHub Actions for automated building and testing.

The workflow file is located at `.github/workflows/build.yml`

### What it does:
- Triggers on every push and pull request to `main` branch
- Sets up JDK 17
- Builds the app using Gradle (`assembleDebug`)
- Uploads the built APK as a downloadable artifact

### How to view the build:
1. Go to the GitHub repository
2. Click on **Actions** tab
3. Click on the latest workflow run
4. Download the APK from the **Artifacts** section

---

## 🗄️ Database
- **Technology:** SQLite via Android `SQLiteOpenHelper`
- **Tables:** Users, Categories, Expenses, Savings Goals
- **No internet required** — fully offline

---

## 📹 Demo Video
> Link will be added here after recording

---

## 🚀 How to Run
1. Clone the repository: `git clone https://github.com/Anda-AmaBhele/Thrive.git`
2. Open in Android Studio
3. Build and run on an Android device (API 24+)

---

## 📚 References
- Android Developers. (2026). SQLite database overview. https://developer.android.com/training/data-storage/sqlite
- Material Design. (2026). Material Design 3 guidelines. https://m3.material.io/
- MPAndroidChart. https://github.com/PhilJay/MPAndroidChart
## Research, Planning & Design
See [Research, Planning and Design Report](docs/Research_Planning_Design_Report.pdf) for app research, design decisions, screen wireframes, and project plan.
