# SMSServer Configuration

## Gateways

You should pre-configure the gateways which will be used. This is done by adding rows in the `smslib_gateways` table.

The fields you should fill are:

* `class` the fully classified class name of the SMSLib Gateway you will use. For example, if you work with serial modems, the value should be `org.smslib.gateway.modem.Modem`.
* `gateway_id` the id (unique name) that identifies this gateway.
* `p0` ... `p5` the parameters of the gateway. These are directly mapped to the constructor parameters of each gateway. More information is given further down.
* `sender_address` the custom originator address that this gateway should use.
* `is_enabled` **0** means the gateway will **not** be used, **1** means the gateway **will** be used.
