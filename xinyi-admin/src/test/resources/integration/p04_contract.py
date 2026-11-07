"""核对P04六个真实HTTP响应与主OpenAPI，保留nullable及不透明渠道ID等约束。"""
from pathlib import Path
import sys
import json
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
    ("refund", "get", "/api/admin/refunds/{refundNo}"),
    ("query", "post", "/api/admin/refunds/{refundNo}/query"),
    ("retry", "post", "/api/admin/refunds/{refundNo}/retry"),
    ("reconciliation", "get", "/api/admin/reconciliation"),
    ("recheck", "post", "/api/admin/reconciliation/{caseId}/query"),
    ("resolve", "post", "/api/admin/reconciliation/{caseId}/resolve"),
]:
    schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
    schema["components"] = spec["components"]
    Draft4Validator(schema, format_checker=FormatChecker()).validate(json.loads((out / (name + ".json")).read_text()))
print("6 actual refund/reconciliation HTTP envelopes conform to primary OpenAPI")
