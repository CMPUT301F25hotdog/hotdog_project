# **CMPUT 301 F25 – Team Hotdog**

## **Team Members**

| Name | CCID | GitHub Username |
|------|------|-----------------|
| Bhuvnesh Batta | bbatta | @BhuvneshBatta|
| Daniel Zhong | dzhong4 | @ShrimpFriedRice04|
| Ethan Carter | ecarter2 | @ethancarter7 |
| Layne pitman | lpitman | @Licensed-Driver |
| Tatsat Shah | tatsat | @tatsatshah05 |
| Vatsal Jain | vatsal2 | @Vatsal-cs |

---

## **Project Description**

- The Event Lottery System Application is an Android application designed to provide fair and accessible registration for high-demand community events. Rather than relying on traditional first-come-first-served signups, the application implements a lottery-based selection system to ensure equitable participation opportunities for all users.

- Event organizers can create and manage events, establish registration periods, and allow entrants to join waiting lists. Upon closure of the registration period, the system employs a randomized selection algorithm to choose participants and deliver automated notifications. 

- Selected entrants may then accept or decline their invitations, with the system automatically conducting replacement draws to fill any declined positions.

- Administrators maintain system oversight with capabilities to browse and manage all events, user profiles, images, and notifications, ensuring platform integrity and compliance with community standards while facilitating seamless interaction among organizers, entrants, and administrators within a unified platform.

---

## **Key Features**

- **Lottery-Based Selection:**  
  Replaces first-come-first-served signups with a fair random draw for event participation. Entrants only need to join the waiting list before registration closes.

- **Event Management for Organizers:**  
  Organizers can create and manage events, set registration deadlines, event capacities, and draw winners directly from the app.

- **QR Code Scanning:**  
  Entrants can scan promotional QR codes to view event details and join waiting lists instantly.

- **Notification System:**  
  Automatic notifications are sent when entrants are selected ("won the lottery") or not selected ("not drawn"). Organizers can also send updates to waiting, selected, or canceled entrants.

- **Profile Management:**  
  Entrants can maintain personal profiles with editable information and a history of past lottery results.

- **Geolocation Verification (Optional):**  
  Organizers can enable location verification to ensure entrants are joining from a specific geographic area.

- **Firebase Integration:**  
  Real-time data storage for events, entrants, and notifications. Ensures instant updates across devices.

- **Accessibility and Fairness:**  
  Designed for users who cannot constantly monitor registration times — providing equitable chances for all participants.

---

## **Tech Stack**

### **Development Environment**
- **IDE:** Android Studio
- **Language:** Java
- **Target SDK:** API 36

### **Architecture & Design**
- **Architecture Pattern:** MVC (Model-View-Controller)
- **Design Tools:** Figma (UI/UX mockups)
- **Modeling:** UML (Unified Modeling Language) for system design
- **UI Layout:** XML layouts with Material Design components

### **Testing**
- **Unit Testing:** JUnit 5
- **UI Testing:** Espresso

---

## **Setup Instructions**

1. **Clone the Repository**
```bash
   git clone https://github.com/CMPUT301F25hotdog/hotdog_project.git
```

2. **Open the Project in Android Studio**
- Open the cloned directory in Android Studio.
- Ensure all Gradle dependencies are synced successfully.

3. **Run the Application**
- Use an emulator or physical Android device to launch the app.
- Log in as an entrant, organizer, or admin to explore role-based functionalities.

---

## **Documentation**

1. [Project Wiki](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki)

- Contains detailed design documentation, including refined requirements, UML diagrams, and discussions.

2. [Scrum Board](https://github.com/orgs/CMPUT301F25hotdog/projects/10)

- Shows the project backlog, sprint progress, team task assignments and story points (according to fibonacci series).

3. [UI Mockups](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki/UI-Mockup)

- Displays the visual layout of the app and storyboard transitions for major user flows.

4. [CRC Cards](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki/CRC-Cards)

- Documents the core classes, their responsibilities, and collaborators used in the system's design.

5. [Story Board](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki/Story-Board)

- Shows the screen flow of the project and user interactions, showing how users navigate between screens and features.

6. [Meeting Minutes](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki/Meeting-Minutes)

- Contains weekly meeting logs, action items, decisions, assigned tasks, and progress tracking for each sprint.

7. [UML Diagram](https://github.com/CMPUT301F25hotdog/hotdog_project.wiki.git)
   
- Unified Modeling Language diagram documenting class structures, relationships, and system architecture.

8. [Project Demo Video](https://github.com/CMPUT301F25hotdog/hotdog_project/wiki/Video_Demo)
   
- Comprehensive video demonstration of application features and functionality.
