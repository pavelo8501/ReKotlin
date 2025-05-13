# Exposify

**Exposify** is a Kotlin library built on top of JetBrains' [Exposed](https://github.com/JetBrains/Exposed) SQL framework. It provides an abstraction layer over Exposed DAO and DSL to simplify and streamline the creation of DTO-based, coroutine-friendly data access logic.

## Features

- 🧩 Automatic mapping between entities and DTOs
- 🔄 Seamless one-to-many, one-to-one relationship bindings
- 🧵 Coroutine-compatible transactions and logging
- 🔍 Custom query conditions via DSL-like syntax
- 🛠️ Extensible repositories with pluggable update/select logic

## Getting Started

[//]: # (To include Exposify in your project, add the following dependency &#40;once published&#41;:)

[//]: # (```kotlin)

[//]: # (dependencies {)

[//]: # (    implementation&#40;"com.yourdomain:exposify:<version>"&#41;)

[//]: # (})

[//]: # (```)

[//]: # (> 📌 Note: This library uses JetBrains' Exposed under the hood. You do **not** need to add Exposed manually if it's included via `api&#40;&#41;` or `implementation&#40;&#41;` in Exposify.)

[//]: # (---)

## License

This project is licensed under the **Apache License 2.0**.

### Third-Party Notices

This project includes software developed by JetBrains s.r.o.:

**Exposed** - https://github.com/JetBrains/Exposed

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---

## Contribution

Contributions and issues are welcome! Feel free to fork, open a PR, or start a discussion.