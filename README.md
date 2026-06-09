xeger
=====

A Java library that generates random strings guaranteed to match a given regular expression — the reverse of a regex matcher.

Originally based on the [Google Code project](http://code.google.com/p/xeger/) by Wilfred Springer.

### Code status

* [![Build Status](https://travis-ci.org/robertrv/xeger.png?branch=master)](https://travis-ci.org/robertrv/xeger)
* [![Coverage Status](https://coveralls.io/repos/robertrv/xeger/badge.png?branch=master)](https://coveralls.io/r/robertrv/xeger?branch=master)

---

## Requirements

- Java 8 or later
- Maven 3.x

---

## Library usage

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>nl.flotsam</groupId>
    <artifactId>xeger</artifactId>
    <version>0.1</version>
</dependency>
```

### Basic generation

```java
Xeger xeger = new Xeger("[a-z]{5,10}");
String result = xeger.generate();
// e.g. "gkpmt"
```

### Reproducible output with a seed

```java
Xeger xeger = new Xeger("[a-z]{5,10}", new Random(42));
String result = xeger.generate();
// always produces the same output for the same seed
```

### Length-bounded generation

```java
Xeger xeger = new Xeger("a*b+");
String result = xeger.generate(5, 10); // best-effort min/max length
```

### Supported regex features

| Feature | Supported |
|---|---|
| Character classes `[abc]`, `[a-z]`, `[^abc]` | ✅ |
| Alternation `a\|b` | ✅ |
| Quantifiers `*`, `+`, `?`, `{n}`, `{n,m}` | ✅ |
| Grouping `(abc)` | ✅ |
| Any character `.` | ✅ |
| Predefined classes `\d`, `\w`, `\s` | ❌ |
| Boundary matchers `^`, `$`, `\b` | ❌ |
| Union/intersection `[a-d[m-p]]`, `[a-z&&[def]]` | ❌ |
| POSIX classes `\p{Lower}`, `\p{Digit}` | ❌ |

---

## CLI usage

Xeger includes a command-line interface for quick interactive testing of patterns.

### Run a pattern

```bash
mvn exec:java -Dexec.args="[a-z]{5}" -q
# e.g. pzgej
```

### Generate multiple strings

```bash
mvn exec:java -Dexec.args="-n 5 [A-Z]{3}[0-9]{4}" -q
# LYB1090
# FFM7004
# NTR3256
# VXV6640
# OTW5010
```

### Multiple patterns at once

```bash
mvn exec:java -Dexec.args="-n 3 '[a-z]+' '[0-9]{4}'" -q
```

### Reproducible output

```bash
mvn exec:java -Dexec.args="--seed 42 -n 3 [a-zA-Z]{8}" -q
```

### Length-bounded output

```bash
mvn exec:java -Dexec.args="--min-length 5 --max-length 10 'a*b+'" -q
```

### All options

```
Usage: xeger [-hV] [--max-length=<maxLength>] [--min-length=<minLength>]
             [-n=<count>] [--seed=<seed>] [PATTERN...]

      [PATTERN...]               Regular expression pattern(s) to generate strings for.
  -n, --count=<count>            Number of strings to generate per pattern (default: 1).
      --min-length=<minLength>   Desired minimum length of generated strings (best effort).
      --max-length=<maxLength>   Desired maximum length of generated strings (best effort).
      --seed=<seed>              Random seed for reproducible output.
  -h, --help                     Show this help message and exit.
  -V, --version                  Print version information and exit.
```

---

## Building from source

```bash
git clone https://github.com/robertrv/xeger.git
cd xeger
mvn package -Dlicense.skip=true
```

Run all tests:

```bash
mvn test -Dlicense.skip=true
```
