# Практическая работа №3-4 — вариант 7

Индивидуальное правило: ставка НДС = 40%.

## Сборка и SelfCheck

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr01.SelfCheck
```

## Запуск программы

```bash
java -cp out ru.edu.pr01.Main
```

На Windows можно собрать так:

```bat
mkdir out
javac -encoding UTF-8 -d out src\ru\edu\pr01\*.java
java -cp out ru.edu.pr01.SelfCheck
java -cp out ru.edu.pr01.Main
```
