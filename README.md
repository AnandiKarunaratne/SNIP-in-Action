# SNIP in Action: A Case Study on the Stability of Process Discovery Algorithms under Noise

This project presents a case study on evaluating the robustness of process discovery algorithms under various types and levels of noise. The analysis is conducted using **[SNIP](https://github.com/AnandiKarunaratne/SNIP)**, a noise injection tool, alongside the **PM4Py** process mining library.


## 📌 Overview

The objective of this study is to assess how well three widely-used process discovery algorithms perform when exposed to controlled noise:

- **Alpha Miner**
- **Heuristics Miner**
- **Inductive Miner**

We use **real-life, publicly available event logs** and inject noise to simulate real-world data quality issues. The resulting models are evaluated for stability and performance.


## 🧪 Case Study Overview

- **Noise Injection Tool**: [SNIP](https://github.com/AnandiKarunaratne/SNIP) (Structured Noise Injection Programme)
- **Datasets**: Publicly available real-life event logs (included in the `Dataset/` folder)
- **Discovery Algorithms**: Alpha Miner, Heuristics Miner, Inductive Miner (via `PM4Py`)
- **Noise Types**:  
  - Insertion  
  - Absence  
  - Ordering  
  - Substitution  
  - Mixed
- **Noise Levels**:  
  - 0% (clean)  
  - 0.5%  
  - 1.0%  
  - 1.5%  
  - 2.0%

## 📁 Project Structure
```
.
├── Dataset/ # Input event logs used for experiments
├── NoiseInjectionWithSNIP/ # Java code for noise injection using SNIP
├── ModelDiscovery/ # Python code for discovering process models using PM4Py
└── evaluation_results.csv # Results
```
