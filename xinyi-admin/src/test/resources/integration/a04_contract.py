"""核对 A04 六个真实 HTTP 响应与主 OpenAPI，不使用手写假响应替代服务端输出。"""
from pathlib import Path
import json
import sys
import yaml
from jsonschema import Draft4Validator, FormatChecker

root, out = map(Path, sys.argv[1:])


def adapt(value):
    if isinstance(value, list):
        return [adapt(item) for item in value]
    if not isinstance(value, dict):
        return value
    result = {key: adapt(item) for key, item in value.items()}
    return {"anyOf": [result, {"type": "null"}]} if result.pop("nullable", False) else result


spec = adapt(yaml.safe_load((root / "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml").read_text()))
for name, method, path in [
    ("list", "get", "/api/admin/drivers"),
    ("detail", "get", "/api/admin/drivers/{driverId}"),
    ("patch", "patch", "/api/admin/drivers/{driverId}"),
    ("disable", "post", "/api/admin/drivers/{driverId}/disable-new-assignments"),
    ("enable", "post", "/api/admin/drivers/{driverId}/enable-new-assignments"),
    ("assignments", "get", "/api/admin/drivers/{driverId}/assignments"),
]:
    schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
    schema["components"] = spec["components"]
    Draft4Validator(schema, format_checker=FormatChecker()).validate(
        json.loads((out / (name + ".json")).read_text()))
print("6 actual driver HTTP responses conform to primary OpenAPI")
