# AutoExposed - Kotlin SQL Library
_AutoExposed_ is a SQL library wrapping [JetBrains/Exposed](https://github.com/JetBrains/Exposed).  
It generates _Exposed_ codes by using JSR-369(Annotation Processing).
Currentry, it supports generating Table object but will support generating CRUD codes.

_**WARNING**: AutoExposed is under heavy development._

# Define Table


```kotlin
@Table
data class Person(
    @PrimaryKey(autoIncrement = true) val id: Long,
    @Column(length = 256, index = true) val name: String,
    @Column val age: Int,
    @Column(length = 256) val job: String?)
```
AutoExposed generates the following _object_ based on the above data class.

```kotlin
object PersonTable : Table("person") {
    val id: Column<Long> = long("id").primaryKey().autoIncrement()
    val name: Column<String> = varchar("name", length = 256).index()
    val age: Column<Int> = integer("age")
    val job: Column<String?> = varchar("job", length = 256).nullable()
}

```
