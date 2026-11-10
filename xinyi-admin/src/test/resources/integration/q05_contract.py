"""真实Q05 ADMIN响应双源校验；本人UNKNOWN设备进度另校验当前主源，不能吞未知状态。"""
from pathlib import Path
import json
import sys
import yaml
from jsonschema import Draft4Validator, FormatChecker


def adapt(value):
    if isinstance(value, list):
        return [adapt(x) for x in value]
    if not isinstance(value, dict):
        return value
    result = {key: adapt(item) for key, item in value.items()}
    return {"anyOf": [result, {"type": "null"}]} if result.pop("nullable", False) else result


root, directory = map(Path, sys.argv[1:3])
mode = sys.argv[3]
operations = [("list", "get", "/api/admin/driver-qualifications"),
              ("detail", "get", "/api/admin/driver-qualifications/{applicationId}"),
              ("audit", "post", "/api/admin/driver-qualifications/{applicationId}/audit")]
sources = ["docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml", "docs/vehicle/V6.4/12_工程执行/task_packets/Q05/openapi.yaml"]
if mode == "unknown":
    operations = [("qualification-unknown", "get", "/api/app/driver/qualification")]
    sources = sources[:1]
for source in sources:
    spec = adapt(yaml.safe_load((root / source).read_text()))
    for name, method, path in operations:
        schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
        schema["components"] = spec["components"]
        Draft4Validator(schema, format_checker=FormatChecker()).validate(json.loads((directory / f"{name}.json").read_text()))
    print(f"{len(operations)} actual {mode} responses conform to {source}")
