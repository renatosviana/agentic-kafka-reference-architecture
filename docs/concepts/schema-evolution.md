# What is schema evolution (in simple terms)

Schema evolution means you can change the structure of your data over time without breaking running systems.

In real systems, data formats never stay the same:

- new fields are added

- old fields become optional

- some fields are deprecated

Schema evolution defines rules that allow producers and consumers to keep working even when data changes.

#### Example

Imagine you start with this event:
```json
{
  "accountId": "ACC123",
  "amount": 50
}
```

Later, the business needs a new field:
```json
{
  "accountId": "ACC123",
  "amount": 50,
  "currency": "CAD"
}

```
**Corresponding Avro field added to the existing record (breaking change):**
```json
{
  "name": "currency",
  "type": "string"
}
 ```
#### Runtime failure caused by breaking schema evolution

When sending a request via Postman, the application fails while converting the request payload into an Avro record because the new field `currency` was added without a default value.
<img width="1426" height="523" alt="image" src="https://github.com/user-attachments/assets/e9c86792-5705-493f-8f5f-99de0a35f0cb" />

**This simulates a real production deployment where a new service version is deployed while existing producers, consumers, or request payloads do not yet include the new field.**

The failure occurs during Avro record construction (before the message is sent to Kafka):

## Request-to-Kafka Avro Serialization Flow
```
Postman JSON
   ↓
Spring Controller
   ↓
Avro Builder (generated class)
   ↓
Kafka Avro Serializer
   ↓
Schema Registry
```
When adding a new field (e.g., `currency`) **without a default value**, producing an event using the generated Avro `Builder` fails because Avro cannot supply a default for the missing field.

```
Path in schema: --> currency
        at org.apache.avro.generic.GenericData.getDefaultValue(GenericData.java:1286)
        at org.apache.avro.data.RecordBuilderBase.defaultValue(RecordBuilderBase.java:138)
        at com.viana.avro.AccountEvent$Builder.build(AccountEvent.java:607)
        at com.viana.poc.controller.AccountController.credit(AccountController.java:36)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
```
**Key failure point:**  
`AccountEvent$Builder.build(AccountEvent.java:607)`

#### Fix: make the new field backward compatible

To safely evolve the schema, the new field was made nullable and a default value was provided:

**Corresponding Avro field (compatible change):**
```json
{
  "name": "currency",
  "type": ["null", "string"],
  "default": null
}
```

## Why this works:

- Older messages do not contain the currency field.

- Avro uses the default value (null) when reading older data.

- The Avro Builder no longer throws an exception when the field is not set.

- The schema remains backward compatible and is accepted by Schema Registry.

#### Result:

- POST requests sent via Postman succeed.
- Events are serialized and published to Kafka.
  <img width="1032" height="560" alt="image" src="https://github.com/user-attachments/assets/3999d6db-77cc-4957-8f78-bbfc9cc03064" />

**Without schema evolution:**

- older consumers may crash

- newer producers may break older apps

- teams are forced to upgrade everything at once

**With schema evolution:**

- old consumers safely ignore currency

- new consumers can use it

- systems evolve independently

## Why Avro + Schema Registry matters here

When using Avro with a Schema Registry:

- every message follows a registered schema

- compatibility rules are enforced automatically

- breaking changes are rejected before they reach production

This guarantees:

- Correctness: consumers always know what fields exist and their types

- Performance: compact binary format, efficient at scale

- Safe evolution: fields can be added/removed in controlled ways

## Why this is critical for domain events

Domain events (like account credits, debits, balances):

- represent facts

- are consumed by many systems

- must not change unpredictably

Schema evolution allows these facts to evolve without outages.
