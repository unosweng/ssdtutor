# SSDTutor: A feedback-driven intelligent tutoring system for secure software development

**FireBugs** (research prototype for **SSDTutor**) is an Eclipse-based Intelligent Tutoring System (ITS) designed to assist developers in secure software development. It automatically detects cryptographic API misuses, suggests repairs, and provides educational feedback to help users understand underlying security vulnerabilities.

This tool covers eight common cryptographic usage patterns, including weak encryption algorithms, weak hash functions, and insecure random number generation.

## 📺 Demo Video

**[Watch the FireBugs Setup and Usage Demo](https://unomaha.yuja.com/V/PlayList?node=63816520&a=76653178)**

---

## 📋 Table of Contents

1. [Prerequisites](https://www.google.com/search?q=%23prerequisites)
2. [Installation & Setup](https://www.google.com/search?q=%23installation--setup)
3. [Configuration](https://www.google.com/search?q=%23configuration)
4. [Running the Application](https://www.google.com/search?q=%23running-the-application)
5. [Usage Guide](https://www.google.com/search?q=%23usage-guide)
6. [Key Features](https://www.google.com/search?q=%23key-features)
7. [Citation](https://www.google.com/search?q=%23citation)

---

## 🛠 Prerequisites

Successful execution requires specific environment versions. Please adhere to the following:

* **Java Development Kit (JDK) 11**:
* Ensure `JAVA_HOME` is set to your JDK 11 installation.
* [Download JDK 11 Archive](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)


* **Eclipse IDE for RCP and RAP Developers**:
* **Required Version:** 2021-06 (R)
* [Download Eclipse 2021-06](https://www.eclipse.org/downloads/packages/release/2021-06/r/eclipse-ide-rcp-and-rap-developers)
* *Tip: Extract to a short path (e.g., `c:\dev\eclipse`) to avoid Windows path length issues.*



---

## 🚀 Installation & Setup

### 1. Initialize Eclipse

1. Launch Eclipse and create a new workspace (e.g., `c:\data\workspace`).
2. **Prepare the UI**:
* Close the "Welcome" tab.
* Open the **Java Perspective** (*Window > Perspective > Open Perspective > Java*).
* Close unneeded views (Task List, Outline) to declutter the interface.



### 2. Clone the Repository

Open a terminal and clone the project:

```bash
git clone https://git.unl.edu/firebug/firebugs-repair.git

```

### 3. Import Projects

1. In Eclipse, select **File > Import...**
2. Choose **General > Existing Projects into Workspace**.
3. Browse to the cloned directory: `firebugs-repair/bugdetectionrepair/eclipseworkspace`
4. Select and import both projects:
* `security-bug-detector`
* `ChangeDistiller`



### 4. Resolve Build Errors (If Necessary)

If `ChangeDistiller` shows errors:

1. Right-click the `ChangeDistiller` project > **Properties**.
2. Navigate to **Java Compiler**.
3. Check **Enable project specific settings**.
4. Set **Compiler compliance level** to **1.8**.

### 5. Install Zest SDK

FireBugs uses Zest for Control Flow Graph (CFG) visualization.

1. Go to **Help > Install New Software...**
2. Work with: `--All Available Sites--`.
3. Filter for: `zest`.
4. Select **Modeling > Zest SDK**.
5. Install and restart Eclipse.

---

## ⚙️ Configuration

1. Go to **Run > Run Configurations...**
2. Right-click **Eclipse Application** and select **New Configuration**.
3. **Main Tab**:
* **Name**: `security-bug-detector`
* **Runtime JRE**: Select JDK 11.


4. **Plug-ins Tab**:
* **Uncheck** "Validate Plug-ins automatically prior to launching".


5. Click **Apply**.

---

## ▶️ Running the Application

1. Click **Run** in the configuration dialog. A new Eclipse instance (Runtime Workspace) will launch.
2. **In the new Runtime window**, perform the following setup:

### Import Security Rules & Data

1. **Import Rules**:
* *File > Import > Existing Projects...*
* Path: `firebugs-repair/bugdetectionrepair/runtime-security-bug-detector/security_rules`


2. **Import Sample Data**:
* *File > Import > Existing Projects...*
* Path: `firebugs-repair/bugdetectionrepair/runtime-security-bug-detector/sampledatasets`
* Select all projects.


3. **Open Detection View**:
* In the *Quick Access* bar (top right), type: `misuse`.
* Select **Crypto Vulnerability Detection & Repair Viewer**.



---

## 📖 Usage Guide

### 1. Detect Vulnerabilities

1. In the top menu, select **Firebugs**.
2. Click **Generate File Path of Type(Preprocess1)**.
3. Click **Find Security Crypto bugs**.
4. Results will appear in the **Detection Tree View**. Click a node to see specific details in the table.

### 2. Repair Misuse

FireBugs utilizes automated program repair based on AST pattern matching.

1. In the *Detection Viewer*, right-click a vulnerability (Type Root or Ind Root).
2. Select the **Repair** option from the context menu.

### 3. Intelligent Tutoring & Feedback

1. Open the **Intelligent Tutoring System** view via Quick Access (`misuse`).
2. Use the **Feedback Recommendation Viewer** to see similar code examples and explanations for why the code is vulnerable.



---

## 🔬 Advanced Features

### Control Flow Graph (CFG) Viewer

Visualizes the flow between the vulnerability indicator and its root cause.

* **Access**: Quick Access > "Control Flow Graph (CFG) Viewer".
* **Usage**: Select an item in the detection table to render its graph.

### Edit Script Viewer (ChangeDistiller)

Displays fine-grained edit operations between the buggy and repaired code versions.

* **Access**: Quick Access > "Edit Script Viewer".
* **Usage**: Right-click the viewer > **Run Change Distiller**. Click table entries to visualize differences side-by-side.

---

## 📄 Citation

If you use this tool in your research, please cite the following paper:

```bibtex
@article{newar2023ssdtutor,
  title={SSDTutor: A feedback-driven intelligent tutoring system for secure software development},
  author={Newar, Dip Kiran Pradhan and Zhao, Rui and Siy, Harvey and Soh, Leen-Kiat and Song, Myoungkyu},
  journal={Science of Computer Programming},
  volume={227},
  pages={102933},
  year={2023},
  publisher={Elsevier},
  doi={10.1016/j.scico.2023.102933}
}

```

---

