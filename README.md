RtReports: Pilot Realtime-Stats
===============================

Introductions
-------------
RtReports poll messages realtime từ Kafka và vẽ lên chart: `Client(s) ====> Kafka ====> RtReports Charts`.

Message format: JSON string
`{
    "a": (string) application name,
    "c": (string) counter name,
    "v": (long) value,
    "t": (long) UNIX timestamp-milliseconds
}`

Ví dụ: `{"a":"kvcm_click","c":"banner_left","v":1,"t":1392372082088}`

- value: nếu không cung cấp, giá trị mặc định là `1`.
- timestamp: timestamp lúc xảy ra message ở client (nơi sinh ra message); nếu không cung cấp thì giá trị mặc định là timestamp của server chạy RtReports.


Download & Install
------------------
Install Sun JDK 1.6+ lên server.

Download binary ở [https://drive.google.com/folderview?id=0B3hT6ig3YjjtSkFhZmNiWWl0U1k&usp=sharing](https://drive.google.com/folderview?id=0B3hT6ig3YjjtSkFhZmNiWWl0U1k&usp=sharing) & unzip.

Chạy application qua script trong thư mục `$RtReports/bin`.

Source code: [https://github.com/btnguyen2k/RtReports](https://github.com/btnguyen2k/RtReports)


Configurations
--------------
Cấu hình của application nằm trong file `$RtReports/conf/application.conf`.

- `kafka.zookeeper.connect_string="10.30.56.131:2181/kafka"`: connection string tới ZooKeeper của Kafka. Chỉnh thành giá trị tương ứng của hệ thống trước khi start application.
- `kafka.consumer.group_id="rtreports"`: để giá trị mặc định.
- `counter.storage="memory"`: để giá trị mặc định.
- Cấu hình danh sách product -> kafka_topic:

```
products {
	# configurations for product named "demo"
	demo {
		kafka_topic = "rtreports_demo"
		counter_names = []
	}
```

Lưu ý:

- Mỗi khi thay đổi danh sách product thì start lại application.
- Tạo topic trong kafka trước khi start application!
- counter_names là 1 array of string, ví dụ `["banner_left", "banner_right"]`. Nếu để empty (`[]`) thì application khi parse message sẽ tự động thêm counter name vào danh sách.
