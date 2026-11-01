// Generated from V6.4 OpenAPI; HTTP DTOs only. Service validates XOR/CAS/authorization.
export type Ack = { "accepted": boolean; "requestId"?: string; "version"?: number };

export type VersionInput = { "expectedVersion": number; "remark"?: string };

export type VersionReason = { "expectedVersion": number; "reason": string };

export type Location = { "lng": number; "lat": number; "accuracyMeter"?: number; "capturedAt": string; "coordinateSystem": "GCJ02" };

export type Address = { "addressId": string; "label": string; "address": string; "location": GeoPoint; "isDefault": boolean; "version": number };

export type LoginRequest = { "code": string };

export type PhoneRequest = { "phoneCode": string; "expectedAccountVersion": number; "verificationRequestId": string };

export type Session = { "sessionId": string; "accountId": string; "activeRole": MiniRole; "availableRoles": Array<MiniRole>; "contextVersion": number; "phoneBound": boolean; "permissions"?: Array<PermissionCode>; "capabilities": Array<CapabilityCode>; "stationIds"?: Array<string>; "stationId"?: (string) | null; "workAccessDisabled"?: boolean; "driverQualification": ((QualificationStatus)) | null; "token": string; "expiresIn": number; "refreshToken": string; "refreshExpiresIn": number; "employeeLink": EmployeeLinkPublic; "accountVersion": number };

export type RoleSwitch = { "role": "OWNER" | "DRIVER" | "STATION"; "expectedContextVersion": number };

export type StationSwitch = { "stationId": string; "expectedContextVersion": number };

export type ProfilePatch = { "nickname"?: string; "realName"?: string; "avatarFileId"?: string; "defaultAddressId"?: string; "expectedVersion": number };

export type Profile = { "accountId": string; "nickname": (string) | null; "phoneMask": (string) | null; "driverQualification": ((QualificationStatus)) | null; "availableRoles": Array<MiniRole>; "permissions": Array<PermissionCode>; "version": number; "realName": (string) | null; "avatarFileId": (string) | null; "defaultAddressId": (string) | null; "employeeLink": EmployeeLinkPublic };

export type SmsSend = { "phone": string; "scene": "BIND_PHONE" };

export type SmsVerify = { "challengeId": string; "code": string };

export type DictItem = { "code": string; "label": string; "sort"?: number; "enabled": boolean; "version"?: number };

export type Vehicle = { "vehicleId": string; "plateNo": string; "vehicleTypeCode": string; "vehicleTypeLabel"?: string; "brandModel"?: string; "version": number; "isDefault"?: boolean };

export type Slot = { "slotId": string; "startAt": string; "endAt": string; "displayText"?: string; "candidateOnly": boolean; "status": "CANDIDATE" | "AVAILABLE" | "FULL" | "CLOSED"; "remainQuota"?: number };

export type QuoteRequest = ({ "slotId": string; "vehicleId"?: string; "vehicleDraft"?: VehicleCreate; "addressId"?: string; "addressDraft"?: AddressCreate; "inspectionTypeCode": string } & (unknown | unknown) & (unknown | unknown));

export type FeeItem = { "feeCode": string; "name": string; "amount": number; "calculation"?: string };

export type Quote = { "quoteId": string; "expiresAt": string; "stationId": string; "stationName": string; "slotId": string; "routeDistanceMeter": number; "routeBasis": "ONE_WAY" | "ROUND_TRIP"; "items": Array<FeeItem>; "totalAmount": number; "priceRuleVersion": number; "termsVersion": string; "quotaPlanVersion": number };

export type OrderCreate = { "quoteId": string; "termsVersion": string; "privacyVersion": string; "acceptedTerms": true; "remark"?: string };

export type Prepay = { "timeStamp": string; "nonceStr": string; "package": string; "signType": string; "paySign": string; "payExpireAt"?: string };

export type Payment = { "merchantTradeNo"?: string; "payStatus"?: "PENDING" | "SUCCESS" | "CLOSED" | "UNKNOWN"; "paidAmount"?: number; "paidAt"?: string; "queryRequired"?: boolean };

export type ActionRequest = { "assignmentId": string; "expectedVersion": number; "location": Location; "reason"?: string };

export type DriverCancel = { "assignmentId": string; "expectedVersion": number; "reasonCode": string; "reason": string };

export type DriverCancelResult = { "order": OrderActionReceipt; "assignmentEnded": boolean; "dailyCancelCount"?: number; "penaltyRound"?: string; "canReceiveNewInvite"?: boolean; "penaltyIds": Array<string> };

export type HandoverCreate = { "assignmentId": string; "type": HandoverType; "expectedVersion": number };

export type Handover = { "handoverId": string; "type": HandoverType; "status": HandoverStatus; "holderRole"?: string; "verifierRole"?: string; "expiresAt"?: string; "confirmedAt"?: string; "version"?: number };

export type HandoverSecret = { "handoverId": string; "code": string; "expiresAt": string };

export type HandoverConfirm = { "expectedOrderVersion": number; "assignmentId": string; "code": string; "evidenceGroupId": string; "location": Location; "itemsConfirmed": Array<string>; "remark"?: string };

export type HandoverResult = { "handover": Handover; "order": OrderActionReceipt; "driverReleased": boolean };

export type PhotoDraft = { "fileId": string; "photoType": PhotoType; "capturedAt": string; "location": Location; "clientCaptureId": string };

export type EvidenceGroup = { "groupId"?: string; "node"?: 1 | 2 | 3 | 4; "status"?: EvidenceStatus; "missingTypes"?: Array<string>; "mileageKm"?: string; "photoIds"?: Array<string>; "version"?: number };

export type MileageInput = { "mileageKm": string; "expectedVersion": number; "reason"?: string; "dashboardPhotoId": string };

export type QualificationDraft = { "expectedVersion": number; "realName"?: string; "idCardNo"?: string; "idCardFrontFileId"?: string; "idCardBackFileId"?: string; "licenseMainFileId"?: string; "licenseSubFileId"?: string; "licenseClass"?: string; "firstIssueDate"?: string; "validFrom"?: string; "validTo"?: string; "longTermValid"?: boolean; "consentVersion"?: string };

export type Qualification = { "applicationId": string; "status": QualificationStatus; "items": Array<string>; "rejectReasons": Array<string>; "reviewDueAt"?: string; "canAcceptOrder"?: boolean; "version": number };

export type QualificationAudit = { "expectedVersion": number; "decision": "APPROVE" | "REJECT"; "opinion": string; "reviewDueAt"?: string };

export type Answer = { "questionId": string; "answerCodes": Array<string> };

export type TrainingSubmit = { "attemptId": string; "answers": Array<Answer>; "expectedVersion": number };

export type Listening = { "enabled": boolean; "expectedVersion": number };

export type Availability = { "listeningEnabled"?: boolean; "qualificationValid"?: boolean; "occupationType"?: OccupationType; "holderId"?: string; "availableForNewOrder"?: boolean; "reason"?: string; "version"?: number; "activePenaltyIds"?: Array<string> };

export type Invite = { "inviteId": string; "orderNo": string; "assignmentId"?: string; "pickupArea"?: string; "pickupSlotStartAt"?: string; "pickupSlotEndAt"?: string; "distanceMeter"?: number; "expiresAt": string; "status": InviteStatus; "version": number };

export type InviteResponse = { "decision": "ACCEPT" | "REJECT"; "expectedVersion": number; "reason"?: string };

export type ManualInvite = { "orderNo": string; "driverId": string; "expectedOrderVersion": number; "reason": string };

export type DispatchConfig = { "dispatchLeadMinutes": number; "inviteResponseSeconds"?: number; "radiusMeter"?: number; "expandedRadiusMeter"?: number; "overallDeadlineSeconds"?: number; "roundPolicy"?: string; "locationMaxAgeSeconds"?: number; "dailyCancelLimit"?: number; "expectedVersion": number };

export type Station = { "deptId"?: string; "stationId"?: string; "name"?: string; "address"?: string; "location"?: GeoPoint; "phoneMask"?: string; "enabled"?: boolean; "supportedVehicleTypeCodes"?: Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number; "version": number };

export type StationGrant = { "ownStationId"?: (string) | null; "authorizedStationIds": Array<string>; "expectedVersion": number; "reason": string };

export type Capacity = { "slotId": string; "stationId": string; "startAt": string; "endAt": string; "quotaType": "PICKUP" | "STATION_RECEIVE"; "total": number; "held": number; "confirmed": number; "consumed": number; "available": number; "version": number; "enabled": boolean; "proposedTotal"?: (number) | null; "canApply"?: (boolean) | null };

export type CapacityChange = { "total": number; "expectedVersion": number; "reason": string; "enabled"?: boolean };

export type LicenseReview = { "expectedVersion": number; "plateNo": string; "ownerName": string; "vin": string; "registerDate": string; "useType": string; "mainFileId": string; "subFileId"?: string; "reviewComment"?: string };

export type License = { "reviewStatus"?: LicenseReviewStatus; "plateNo"?: string; "ownerNameMask"?: string; "vinLast4"?: string; "ocrTaskId"?: string; "confidence"?: Record<string, number>; "version"?: number };

export type ResultItem = { "itemCode": string; "itemLabel"?: string; "result": "PASS" | "FAIL"; "remark"?: string; "evidenceFileIds"?: Array<string> };

export type ResultInput = { "expectedOrderVersion": number; "conclusion": "PASS" | "FAIL"; "items": Array<ResultItem>; "inspectorUserId": string; "inspectedAt": string; "remark"?: string };

export type InspectionResult = { "resultId": string; "orderNo": string; "conclusion": string; "items": Array<ResultItem>; "inspectorName"?: string; "inspectedAt"?: string; "submittedAt"?: string; "source"?: "STATION" | "ADMIN"; "revision": number; "version"?: number };

export type CorrectionInput = { "resultVersion": number; "reason": string; "replacement": ResultInput };

export type Correction = { "correctionId"?: string; "orderNo"?: string; "status"?: CorrectionStatus; "originalRevision"?: number; "replacement"?: ResultInput; "reason"?: string; "version"?: number };

export type Decision = { "expectedVersion": number; "decision": "APPROVE" | "REJECT"; "opinion": string };

export type Refund = { "refundNo"?: string; "orderNo"?: string; "amount"?: number; "status"?: ChannelRefundStatus; "channelRefundId"?: string; "successAt"?: string; "queryRequired"?: boolean; "version"?: number; "caseId"?: string; "reviewRevision"?: number };

export type DisputeInput = { "type": "DAMAGE" | "MILEAGE" | "DELAY" | "FEE" | "SERVICE" | "OTHER"; "description": string; "evidenceFileIds"?: Array<string>; "expectedOrderVersion": number };

export type Dispute = { "disputeId"?: string; "orderNo"?: string; "type"?: string; "description"?: string; "status"?: ServiceCaseStatus; "conclusion"?: string; "createdAt"?: string; "resolvedAt"?: string; "version"?: number };

export type DisputeResolve = { "expectedVersion": number; "conclusion": string; "opinion": string; "requestRefund": boolean };

export type ExceptionInput = { "type": "OWNER_NO_SHOW" | "STATION_REJECT" | "DOCUMENT_MISSING" | "UPLOAD_FAILED" | "CONTACT_FAILED" | "OTHER"; "description": string; "evidenceFileIds"?: Array<string>; "expectedOrderVersion": number };

export type Exception = { "exceptionId"?: string; "orderNo"?: string; "type"?: string; "status"?: ServiceCaseStatus; "description"?: string; "resolution"?: string; "version"?: number };

export type ReviewInput = { "score": number; "tags"?: Array<string>; "content"?: string };

export type Review = { "reviewId"?: string; "orderNo"?: string; "score"?: number; "tags"?: Array<string>; "content"?: string; "createdAt"?: string };

export type PriceRule = { "ruleId"?: string; "revision"?: number; "status"?: PriceRuleStatus; "config"?: PriceUpdate; "version": number };

export type UploadRequest = { "purpose": "VEHICLE_PHOTO" | "ID_CARD" | "DRIVER_LICENSE" | "VEHICLE_LICENSE" | "RESULT_EVIDENCE" | "DISPUTE" | "STATION_QUALIFICATION" | "AVATAR"; "orderNo"?: string; "applicationId"?: string; "fileName": string; "mimeType": "image/jpeg" | "image/png"; "size": number };

export type UploadTicket = { "uploadId": string; "fileId": string; "uploadUrl": string; "expiresAt": string; "requiredHeaders"?: Record<string, string> };

export type FileReady = { "fileId"?: string; "status"?: FileStatus; "serverSha256"?: string; "size"?: number; "version"?: number };

export type AccessRequest = { "purpose": string; "orderNo"?: string; "fields"?: Array<string> };

export type AccessResult = { "requestId"?: string; "status"?: "PENDING" | "APPROVED" | "REJECTED"; "expiresAt"?: string; "downloadUrl"?: string };

export type ExportRequest = { "filters"?: { "stationId"?: string; "startAt"?: string; "endAt"?: string; "status"?: string }; "fields": Array<string>; "reason": string; "type"?: "ORDER" | "VEHICLE" | "OWNER" | "DRIVER" | "DRIVER_STATEMENT" | "AUDIT" };

export type ExportTask = { "taskId"?: string; "status"?: ExportTaskStatus; "expiresAt"?: string; "downloadUrl"?: string };

export type PrivacyRequest = { "type": "CORRECT" | "WITHDRAW" | "DELETE" | "ACCESS"; "description": string };

export type PrivacyCase = { "caseId"?: string; "type"?: string; "status"?: PrivacyCaseStatus; "reason"?: string; "createdAt"?: string; "version"?: number };

export type Notification = { "messageId"?: string; "scene"?: string; "orderNo"?: string; "content"?: string; "readAt"?: string; "createdAt"?: string; "sendStatus"?: string };

export type MessageTemplate = { "templateId"?: string; "scene"?: string; "channel"?: string; "providerTemplateId"?: string; "enabled"?: boolean; "expectedVersion": number };

export type ConfigItem = { "key": string; "value": string };

export type ConfigPatch = { "items": Array<ConfigItem>; "expectedVersion": number; "reason": string };

export type Metric = { "name"?: string; "value"?: number; "definition"?: string; "calculatedAt"?: string };

export type Audit = { "eventId"?: string; "actorId"?: string; "actingRole"?: string; "stationId"?: string; "action"?: string; "targetId"?: string; "at"?: string; "summary"?: string };

export type Timeline = { "eventId"?: string; "orderNo"?: string; "action"?: string; "before"?: string; "after"?: string; "actorName"?: string; "at"?: string; "reason"?: string };

export type Reconcile = { "caseId"?: string; "orderNo"?: string; "type"?: string; "localStatus"?: string; "channelStatus"?: string; "status"?: ReconcileStatus; "version"?: number };

export type EncryptedResource = { "algorithm": string; "ciphertext": string; "nonce": string; "associated_data"?: string; "original_type"?: string };

export type Callback = { "id": string; "create_time": string; "event_type": string; "resource_type": string; "resource": EncryptedResource; "summary"?: string };

export type CallbackFailure = { "code": string; "message": string };

export type JobTask = { "taskId"?: string; "type"?: string; "status"?: string; "attempts"?: number; "nextAttemptAt"?: string; "errorSummary"?: string; "version"?: number };

export type AfterSaleApply = { "kind": "CANCEL_REFUND" | "REFUND_ONLY"; "reasonCode"?: string; "reason": string; "requestedAmount": number; "voucherFileIds"?: Array<string>; "expectedOrderVersion": number };

export type AfterSaleReview = { "expectedVersion": number; "observedOrderVersion": number; "decision": "APPROVE" | "REJECT"; "approvedAmount": number; "opinion": string };

export type AfterSaleReviewPreview = { "caseId": string; "caseVersion": number; "orderVersion": number; "custody"?: "OWNER" | "DRIVER" | "STATION"; "assignmentId"?: string; "paidAmount"?: number; "refundedAmount"?: number; "reservedAmount"?: number; "availableAmount": number; "returnRequired": boolean; "allowedActions"?: Array<AllowedAction>; "warnings"?: Array<string> };

export type AfterSale = { "caseId": string; "orderNo": string; "kind": "CANCEL_REFUND" | "REFUND_ONLY"; "caseStatus": "PENDING_REVIEW" | "EXECUTING" | "CLOSED" | "REJECTED"; "reviewStatus": "PENDING" | "APPROVED" | "REJECTED"; "reviewRevision"?: number; "approvedAmount"?: number; "requestedAmount"?: number; "returnState": "UNDETERMINED" | "NOT_REQUIRED" | "WAIT_RETURN" | "RETURNED"; "refundState": "NONE" | "NOT_REQUIRED" | "WAIT_RETURN" | "READY" | "PROCESSING" | "SUCCESS" | "UNKNOWN" | "ABNORMAL" | "CLOSED"; "custody"?: "OWNER" | "DRIVER" | "STATION"; "fulfillmentStatus"?: string; "assignmentId"?: string; "refundNo"?: string; "opinion"?: string; "version": number; "createdAt"?: string; "updatedAt"?: string; "allowedActions"?: Array<AllowedAction> };

export type AfterSaleNote = { "expectedVersion": number; "note": string; "voucherFileIds"?: Array<string> };

export type AfterSaleReviewPreviewInput = { "expectedVersion": number; "approvedAmount": number };

export type OwnerRecord = { "ownerId": string; "nickname"?: string; "serviceName"?: string; "phoneMask": string; "phoneVerified"?: boolean; "vehicleCount"?: number; "orderCount"?: number; "lastServiceAt"?: string; "serviceFlag"?: "NORMAL" | "VERIFY_AGAIN"; "internalRemark"?: string; "version": number; "allowedActions"?: Array<AllowedAction> };

export type OwnerMaintenance = { "expectedVersion": number; "serviceName"?: string; "internalRemark"?: string; "serviceFlag"?: "NORMAL" | "VERIFY_AGAIN"; "reason": string };

export type VehicleRecord = { "vehicleId": string; "plateNo": string; "plateType"?: string; "vehicleTypeCode": string; "vehicleTypeLabel"?: string; "brandModel"?: string; "vinMask"?: string; "registeredAt"?: string; "useType"?: string; "ownerRelation"?: OwnerRecord; "verificationStatus"?: "PENDING" | "VERIFIED"; "lastResult"?: string; "lastInspectAt"?: string; "version": number; "allowedActions"?: Array<AllowedAction> };

export type VehicleMaintenance = { "expectedVersion": number; "brandModel"?: string; "vehicleTypeCode"?: string; "useType"?: string; "registeredAt"?: string; "reason": string };

export type DriverRecord = { "driverId": string; "accountId"?: string; "name"?: string; "phoneMask"?: string; "qualificationStatus": string; "newAssignmentEnabled"?: boolean; "listening"?: boolean; "assignmentId"?: string; "occupied": boolean; "licenseClass"?: string; "licenseValidTo"?: string; "reviewDueAt"?: string; "completedAssignmentCount"?: number; "score"?: string; "internalRemark"?: string; "version": number; "allowedActions"?: Array<AllowedAction> };

export type DriverMaintenance = { "expectedVersion": number; "internalRemark"?: string; "reason": string };

export type DriverFeeRule = { "ruleId": string; "revision": number; "status": DriverFeeRuleStatus; "configuration"?: DriverFeeRuleUpdate; "publishedBy"?: string; "version": number };

export type DriverFeeTrial = { "rule": DriverFeeRuleUpdate; "contractDistanceMeter": number };

export type DriverFeeTrialResult = { "baseAmount": number; "distanceAmount": number; "totalAmount": number; "formulaDescription"?: string };

export type DriverFeeEntry = { "entryId": string; "driverId": string; "payorEntityId"?: string; "assignmentId": string; "orderNo"?: string; "sourceEventId"?: string; "entryKind"?: "NORMAL_RETURN" | "CANCEL_RETURN" | "CANCEL_BEFORE_PICKUP" | "ADJUSTMENT" | "INITIAL_VALUATION" | "PRICE_DELTA"; "serviceEndedAt"?: string; "baseAmount"?: (number) | null; "distanceAmount"?: (number) | null; "adjustmentAmount"?: number; "amount"?: (number) | null; "ruleId"?: string; "ruleRevision"?: number; "pricingStatus": "PENDING_PRICING" | "READY"; "version": number };

export type StatementPreview = { "selectedEntries": Array<DriverFeeEntry>; "selectedCount": number; "unpricedCount": number; "knownAmount": number; "amount": (number) | null; "canGenerate": boolean; "canSend"?: boolean; "conflicts": Array<StatementSelectionConflict>; "serviceRangeStart"?: (string) | null; "serviceRangeEndExclusive"?: (string) | null };

export type DriverStatement = { "statementId": string; "driverId": string; "payorEntityId": string; "currency"?: "CNY"; "cutoffAt"?: string; "status": StatementStatus; "revision": number; "snapshotHash"?: string; "entries"?: Array<DriverStatementLine>; "amount": (number) | null; "verifiedPaidAmount"?: number; "driverConfirmedAt"?: string; "version": number; "allowedActions"?: Array<AllowedAction>; "knownAmount": number; "unpricedCount": number; "manualPriceCount": number; "reviewStatus": StatementReviewStatus; "reviewRevision"?: (number) | null; "publishedRevision"?: (number) | null; "hasPaymentRecord"?: boolean; "immutableOriginal"?: boolean; "selectionMode": "EXPLICIT"; "serviceRangeStart"?: (string) | null; "serviceRangeEndExclusive"?: (string) | null; "createdAt"?: string; "paymentSummary": StatementPaymentSummary; "activeEntryCount": number; "statementNo": string };

export type StatementConfirm = { "expectedVersion": number; "snapshotHash": string; "confirmAllEntries": true; "expectedRevision": number };

export type StatementDifference = { "expectedVersion": number; "entryIds"?: Array<string>; "description": string; "evidenceFileIds"?: Array<string> };

export type StatementDifferenceRecord = { "differenceId": string; "statementId": string; "driverId"?: string; "entryIds"?: Array<string>; "description"?: string; "status": "OPEN" | "RESOLVED"; "resolution"?: string; "version": number };

export type DifferenceResolve = { "expectedVersion": number; "statementVersion": number; "resolution": string; "approvedAdjustmentIds"?: Array<string>; "resend": boolean };

export type DriverAdjustmentApply = { "sourceEntryId": string; "amount": number; "reason": string; "evidenceFileIds": Array<string>; "statementId"?: string };

export type DriverAdjustment = { "adjustmentId": string; "driverId"?: string; "sourceEntryId": string; "amount": number; "reason"?: string; "status": "PENDING" | "APPROVED" | "REJECTED"; "generatedEntryId"?: string; "version": number };

export type ExternalPaymentApply = { "statementId": string; "statementVersion": number; "amount": number; "externalReference": string; "paidAt": string; "evidenceFileIds": Array<string>; "reason"?: string };

export type ExternalPaymentRecord = { "recordId": string; "statementId": string; "driverId"?: string; "payorEntityId"?: string; "amount": number; "externalReferenceMask"?: string; "status": ExternalPaymentStatus; "paidAt"?: string; "registeredBy"?: string; "verifiedBy"?: string; "version": number; "evidenceFileIds"?: Array<string>; "verificationOpinion"?: string };

export type DriverAssignmentRecord = { "assignmentId": string; "driverId": string; "orderNo": string; "acceptedAt"?: string; "endedAt"?: string; "endReason"?: string; "custody"?: string; "released"?: boolean; "feeEntryId"?: string };

export type SessionView = { "sessionId": string; "accountId": string; "activeRole": MiniRole; "availableRoles": Array<MiniRole>; "contextVersion": number; "phoneBound": boolean; "permissions"?: Array<PermissionCode>; "capabilities": Array<CapabilityCode>; "stationIds"?: Array<string>; "stationId"?: (string) | null; "workAccessDisabled"?: boolean; "driverQualification": ((QualificationStatus)) | null; "employeeLink": EmployeeLinkPublic; "accountVersion": number };

export type AccessSession = { "sessionId": string; "accountId": string; "activeRole": MiniRole; "availableRoles": Array<MiniRole>; "contextVersion": number; "phoneBound": boolean; "permissions"?: Array<PermissionCode>; "capabilities": Array<CapabilityCode>; "stationIds"?: Array<string>; "stationId"?: (string) | null; "workAccessDisabled"?: boolean; "driverQualification": ((QualificationStatus)) | null; "token": string; "expiresIn": number; "employeeLink": EmployeeLinkPublic; "accountVersion": number };

export type TokenRefreshInput = { "refreshToken": string };

export type SessionLogoutInput = { "allDevices"?: boolean };

export type DriverStatementLine = { "lineId": string; "entryId": string; "assignmentId": string; "orderNo"?: string; "plateNo"?: string; "entryKind"?: "NORMAL_RETURN" | "CANCEL_RETURN" | "CANCEL_BEFORE_PICKUP" | "ADJUSTMENT" | "INITIAL_VALUATION" | "PRICE_DELTA"; "serviceEndedAt"?: string; "referenceAmount"?: (number) | null; "proposedAmount": (number) | null; "pricingSource": PricingSource; "manualChanged"?: boolean; "reason"?: string; "lineVersion": number; "sourceVersion"?: number; "allowedActions"?: Array<AllowedAction> };

export type StatementPriceInput = { "expectedVersion": number; "expectedLineVersion": number; "proposedAmount": number; "reason": string; "evidenceFileIds"?: Array<string> };

export type PriceChangeRecord = { "changeId": string; "statementId"?: string; "lineId": string; "oldAmount": (number) | null; "newAmount": number; "reason": string; "actorUserId"?: string; "revision": number; "createdAt"?: string };

export type StatementRevisionAction = { "expectedVersion": number; "expectedRevision": number; "snapshotHash": string };

export type StatementReviewDecision = { "expectedVersion": number; "expectedRevision": number; "snapshotHash": string; "decision": "APPROVE" | "REJECT"; "opinion": string };

export type StatementWithdraw = { "expectedVersion": number; "expectedRevision": number; "reason": string };

export type StatementReviewRecord = { "reviewId": string; "statementId": string; "revision": number; "snapshotHash"?: string; "status": "PENDING" | "APPROVED" | "REJECTED" | "WITHDRAWN"; "submittedBy"?: string; "reviewedBy"?: (string) | null; "opinion"?: string; "version": number };

export type LogoutResult = { "loggedOut": true; "scope": "CURRENT" | "ALL" };

export type SelectedFeeEntry = { "entryId": string; "sourceVersion": number };

export type StatementSelectionRequest = { "driverId": string; "payorEntityId": string; "currency": "CNY"; "selectedEntries": Array<SelectedFeeEntry>; "filterSnapshot"?: { "serviceStartDate"?: string; "serviceEndDateInclusive"?: string; "keyword"?: string }; "remark"?: string };

export type StatementSelectionConflict = { "entryId": string; "code": "ALREADY_ALLOCATED" | "SOURCE_CHANGED" | "TASK_NOT_ENDED" | "NOT_ELIGIBLE" | "NOT_VISIBLE"; "message": string; "relatedStatementId"?: (string) | null };

export type StatementAddEntries = { "expectedVersion": number; "expectedRevision": number; "selectedEntries": Array<SelectedFeeEntry>; "reason": string };

export type StatementMembershipAction = { "expectedVersion": number; "expectedRevision": number; "reason": string };

export type StatementMembershipChange = { "changeId": string; "statementId": string; "entryId"?: (string) | null; "action": "CREATE" | "ADD" | "REMOVE" | "REACTIVATE" | "VOID"; "revisionBefore"?: number; "revisionAfter": number; "actorUserId"?: string; "reason"?: string; "createdAt": string };

export type StatementPaymentSummary = { "paymentStatus": "NOT_READY" | "UNPAID" | "PARTIAL" | "PAID" | "NO_PAYMENT_REQUIRED" | "OFFSET_REVIEW"; "verifiedPaidAmount": number; "pendingVerificationAmount": number; "remainingPayableAmount": (number) | null; "recordableAmount": number };

export type DriverReconciliationEntry = { "entryId": string; "assignmentId": string; "orderNo"?: string; "plateNo"?: string; "driverId": string; "payorEntityId": string; "currency"?: "CNY"; "entryKind"?: string; "serviceEndedAt"?: string; "sourceAmount"?: (number) | null; "displayAmount"?: (number) | null; "sourceVersion": number; "reconciliationState": "UNBILLED" | "PREPARING" | "AWAIT_DRIVER" | "DISPUTED" | "UNPAID" | "PARTIAL" | "PAID" | "NO_PAYMENT_REQUIRED" | "OFFSET_REVIEW" | "IN_SERVICE"; "isSelectable": boolean; "unselectableReason"?: string; "relatedStatementId"?: (string) | null; "relatedStatementNo"?: (string) | null };

export type DriverReconciliationOverview = { "driverId": string; "payorEntityId": string; "unbilledCount": number; "unpricedCount": number; "preparingServiceCount"?: number; "statementCount": number; "confirmedRemainingAmount": number; "pendingVerificationAmount": number; "knownUnbilledAmount"?: number; "hasUnknownAmount": boolean };

export type DriverOwnReconciliationEntry = { "entryId": string; "assignmentId": string; "orderNo"?: string; "plateNo"?: string; "payorEntityName"?: string; "serviceEndedAt": string; "displayState": string; "publishedStatementId"?: (string) | null; "publishedAmount"?: (number) | null };

export type DriverOwnPaymentSummary = { "recordId": string; "statementId": string; "payorEntityName"?: string; "amount": number; "paidAt": string; "verificationStatus": "PENDING" | "VERIFIED" | "REJECTED" | "REVERSED" };

export type DriverStatementPublic = { "statementId": string; "driverId": string; "payorEntityId": string; "currency": "CNY"; "serviceRangeStart": (string) | null; "serviceRangeEndExclusive": (string) | null; "status": "SENT" | "DISPUTED" | "CONFIRMED"; "revision": number; "snapshotHash": string; "amount": number; "driverConfirmedAt": (string) | null; "version": number; "paymentSummary": DriverStatementPaymentPublic; "entries": Array<DriverStatementLinePublic>; "statementNo": string };

export type ExternalPaymentVerify = { "expectedVersion": number; "decision": "APPROVE" | "REJECT"; "opinion": string };

export type FulfillmentStatus = "PENDING_PAY" | "SCHEDULED" | "PENDING_DISPATCH" | "DISPATCHING" | "MANUAL_DISPATCH" | "ACCEPTED" | "GOING_PICKUP" | "ARRIVED_PICKUP" | "PICKED_UP" | "ARRIVED_STATION" | "INSPECTING" | "INSPECTED" | "CAR_TAKEN" | "WAIT_CONFIRM" | "COMPLETED" | "CANCELLED" | "CLOSED";

export type CancelStatus = "NONE" | "APPLIED" | "REJECTED" | "APPROVED_WAIT_RETURN" | "COMPLETED";

export type CustodyType = "OWNER" | "DRIVER" | "STATION";

export type PaymentStatus = "PENDING" | "SUCCESS" | "CLOSED" | "UNKNOWN";

export type RefundStatus = "NONE" | "NOT_REQUIRED" | "WAIT_RETURN" | "READY" | "PROCESSING" | "SUCCESS" | "UNKNOWN" | "ABNORMAL" | "CLOSED";

export type DisputeStatus = "NONE" | "OPEN" | "PROCESSING" | "RESOLVED";

export type MiniRole = "OWNER" | "DRIVER" | "STATION";

export type QualificationStatus = "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED" | "EXPIRED" | "SUSPENDED";

export type HandoverType = "OWNER_TO_DRIVER" | "DRIVER_TO_STATION" | "STATION_TO_DRIVER" | "DRIVER_TO_OWNER";

export type HandoverStatus = "PENDING" | "CONFIRMED" | "LOCKED" | "EXPIRED";

export type PhotoType = "FRONT" | "BACK" | "LEFT" | "RIGHT" | "DASHBOARD" | "INTERIOR" | "EXTRA";

export type InviteStatus = "WAIT_RESPONSE" | "ACCEPTED" | "REJECTED" | "EXPIRED" | "REVOKED";

export type OrderVehicleView = { "vehicleId": string; "plateNo": string; "vehicleTypeCode": string; "vehicleTypeLabel": string; "brandModel": (string) | null; "vinLast4": (string) | null };

export type OrderStationView = { "stationId": string; "name": string; "publicAddress": string; "location": GeoPoint; "servicePhone": (string) | null };

export type ContactView = { "displayName": string; "phoneMask": (string) | null; "canRequestContact": boolean };

export type OrderAddressView = { "address": string; "location": GeoPoint; "contact": ContactView };

export type DriverIdentityPublic = { "displayName": string; "driverCode": string; "rating": (number) | null; "contact": ContactView };

export type OrderMoneyView = { "items": Array<FeeItem>; "totalAmount": number; "paidAmount": number; "refundedAmount": number };

export type OrderMileageView = { "pickupKm": (string) | null; "returnKm": (string) | null; "distanceKm": (string) | null; "abnormal": boolean; "publicExplanation": (string) | null };

export type OrderAfterSalePublic = { "caseId": string; "kind": "CANCEL_REFUND" | "REFUND_ONLY"; "caseStatus": "PENDING_REVIEW" | "EXECUTING" | "CLOSED" | "REJECTED"; "approvedAmount": (number) | null; "returnState": "UNDETERMINED" | "NOT_REQUIRED" | "WAIT_RETURN" | "RETURNED"; "refundState": RefundStatus; "publicOpinion": (string) | null; "version": number };

export type WorkCancellationView = { "returnRequired": boolean; "displayStatus": string; "publicInstruction": (string) | null };

export type OrderActionReceipt = { "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "custody": CustodyType; "cancelStatus": CancelStatus; "version": number; "assignmentId": (string) | null; "driverReleased": boolean };

export type OwnerOrderView = { "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "view": "OWNER"; "paymentStatus": PaymentStatus; "refundStatus": RefundStatus; "disputeStatus": DisputeStatus; "pickupAddress": OrderAddressView; "returnAddress": OrderAddressView; "driver": ((DriverIdentityPublic)) | null; "dispatchAt": (string) | null; "payExpireAt": string; "autoConfirmAt": (string) | null; "money": OrderMoneyView; "mileage": OrderMileageView; "afterSale": ((OrderAfterSalePublic)) | null; "allowedActions": Array<OwnerOrderAction> };

export type OwnerOrderSummary = { "view": "OWNER"; "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "allowedActions": Array<OwnerOrderAction>; "paymentStatus": PaymentStatus; "money": OrderMoneyView };

export type DriverOrderView = { "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "view": "DRIVER"; "assignmentId": string; "assignmentEndedAt": (string) | null; "accessPhase": "ACTIVE_ASSIGNMENT" | "HISTORY"; "pickupArea": string; "pickupAddress": ((OrderAddressView)) | null; "returnAddress": ((OrderAddressView)) | null; "ownerContact": ((ContactView)) | null; "mileage": OrderMileageView; "cancellation": WorkCancellationView; "driverReleased": boolean; "allowedActions": Array<DriverOrderAction> };

export type DriverOrderSummary = { "view": "DRIVER"; "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "allowedActions": Array<DriverOrderAction>; "assignmentId": string; "assignmentEndedAt": (string) | null; "accessPhase": "ACTIVE_ASSIGNMENT" | "HISTORY"; "driverReleased": boolean };

export type StationOrderView = { "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "view": "STATION"; "paymentStatus": PaymentStatus; "driver": ((DriverIdentityPublic)) | null; "assignmentId": (string) | null; "licenseReviewStatus": "MISSING" | "PENDING" | "VERIFIED"; "result": ("PASS" | "FAIL") | null; "cancellation": WorkCancellationView; "allowedActions": Array<StationOrderAction> };

export type StationOrderSummary = { "view": "STATION"; "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "allowedActions": Array<StationOrderAction>; "paymentStatus": PaymentStatus };

export type AdminOrderView = { "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "view": "ADMIN"; "ownerAccountId": string; "ownerContact": ContactView; "driver": ((DriverIdentityPublic)) | null; "assignmentId": (string) | null; "authoritativeDeptId": string; "paymentStatus": PaymentStatus; "refundStatus": RefundStatus; "disputeStatus": DisputeStatus; "money": ((OrderMoneyView)) | null; "pickupAddress": ((OrderAddressView)) | null; "returnAddress": ((OrderAddressView)) | null; "mileage": OrderMileageView; "afterSale": ((OrderAfterSalePublic)) | null; "payExpireAt": string; "dispatchAt": (string) | null; "autoConfirmAt": (string) | null; "driverReleased": boolean; "allowedActions": Array<AdminOrderAction> };

export type AdminOrderSummary = { "view": "ADMIN"; "orderNo": string; "fulfillmentStatus": FulfillmentStatus; "displayStatus": string; "cancelStatus": CancelStatus; "custody": CustodyType; "vehicle": OrderVehicleView; "station": OrderStationView; "slotStartAt": string; "slotEndAt": string; "returnedAt": (string) | null; "version": number; "allowedActions": Array<AdminOrderAction>; "paymentStatus": PaymentStatus };

export type AfterSalePublic = { "caseId": string; "orderNo": string; "kind": "CANCEL_REFUND" | "REFUND_ONLY"; "caseStatus": "PENDING_REVIEW" | "EXECUTING" | "CLOSED" | "REJECTED"; "reviewStatus": "PENDING" | "APPROVED" | "REJECTED"; "approvedAmount"?: number; "requestedAmount"?: number; "returnState": "UNDETERMINED" | "NOT_REQUIRED" | "WAIT_RETURN" | "RETURNED"; "refundState": "NONE" | "NOT_REQUIRED" | "WAIT_RETURN" | "READY" | "PROCESSING" | "SUCCESS" | "UNKNOWN" | "ABNORMAL" | "CLOSED"; "custody"?: "OWNER" | "DRIVER" | "STATION"; "fulfillmentStatus"?: string; "refundNo"?: string; "version": number; "createdAt"?: string; "updatedAt"?: string; "allowedActions"?: Array<AllowedAction>; "publicOpinion"?: (string) | null };

export type DriverStatementPaymentPublic = { "paymentStatus": "NOT_READY" | "UNPAID" | "PARTIAL" | "PAID" | "NO_PAYMENT_REQUIRED" | "OFFSET_REVIEW"; "verifiedPaidAmount": number; "remainingPayableAmount": (number) | null };

export type AddressCreate = { "label"?: string; "address": string; "location": GeoPoint; "isDefault"?: boolean };

export type AddressUpdate = { "label"?: string; "address": string; "location": GeoPoint; "isDefault"?: boolean; "expectedVersion": number };

export type VehicleCreate = { "plateNo": string; "vehicleTypeCode": string; "brandModel"?: string; "useType"?: string; "vinLast4"?: string; "isDefault"?: boolean };

export type VehicleUpdate = { "plateNo": string; "vehicleTypeCode": string; "brandModel"?: string; "useType"?: string; "vinLast4"?: string; "isDefault"?: boolean; "expectedVersion": number };

export type StationCreate = { "name": string; "address": string; "location": GeoPoint; "contactPhone"?: string; "enabled"?: boolean; "supportedVehicleTypeCodes": Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number; "qualificationFileIds"?: Array<string> };

export type StationUpdate = { "name": string; "address": string; "location": GeoPoint; "contactPhone"?: string; "enabled"?: boolean; "supportedVehicleTypeCodes": Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number; "qualificationFileIds"?: Array<string>; "expectedVersion": number };

export type QualificationCreate = { "consentVersion": string };

export type PenaltyStatus = "ACTIVE" | "EXPIRED" | "REVOKED";

export type PenaltyAppealStatus = "NONE" | "SUBMITTED" | "APPROVED" | "REJECTED";

export type PenaltyType = "SKIP_ROUND" | "STOP_TODAY";

export type Penalty = { "penaltyId": string; "assignmentId": string; "orderNo": string; "type": PenaltyType; "status": PenaltyStatus; "businessDate": string; "reason": string; "ruleVersion": string; "roundKey": (string) | null; "effectiveUntil": (string) | null; "appealStatus": PenaltyAppealStatus; "latestAppealId": (string) | null; "version": number; "allowedActions": Array<AllowedAction> };

export type PenaltyAppeal = { "appealId": string; "penaltyId": string; "status": AppealRecordStatus; "reason": string; "evidenceFileIds": Array<string>; "submittedAt": string; "publicOpinion": (string) | null; "reviewedAt": (string) | null; "version": number };

export type PenaltyAppealCreate = { "expectedVersion": number; "reason": string; "evidenceFileIds"?: Array<string> };

export type TrainingStatus = "NOT_STARTED" | "IN_PROGRESS" | "READY_FOR_EXAM" | "PASSED" | "FAILED";

export type AttemptStatus = "OPEN" | "SUBMITTED" | "PASSED" | "FAILED" | "EXPIRED";

export type TrainingUnit = { "trainingId": string; "courseVersion": number; "title": string; "description": string; "mediaFileId": string; "requiredDurationSeconds": number; "verifiedDurationSeconds": number; "status": TrainingStatus; "latestAttemptId": (string) | null; "version": number };

export type Training = { "applicationId": string; "units": Array<TrainingUnit>; "complete": boolean; "version": number };

export type TrainingProgressInput = { "expectedVersion": number; "courseVersion": number; "sequence": number; "positionSeconds": number; "playbackSessionId": string };

export type TrainingPlayback = { "playbackSessionId": string; "trainingId": string; "courseVersion": number; "serverStartedAt": string; "expiresAt": string; "version": number };

export type QuestionOption = { "code": string; "text": string };

export type TrainingQuestion = { "questionId": string; "type": "SINGLE" | "MULTIPLE" | "TRUE_FALSE"; "prompt": string; "options": Array<QuestionOption> };

export type TrainingAttempt = { "attemptId": string; "trainingId": string; "courseVersion": number; "status": AttemptStatus; "questions": Array<TrainingQuestion>; "startedAt": string; "expiresAt": string; "submittedAt": (string) | null; "score": (number) | null; "passed": (boolean) | null; "version": number };

export type TrainingAttemptCreate = { "expectedVersion": number; "courseVersion": number };

export type TrainingCourseInput = { "title": string; "description"?: string; "mediaFileId": string; "requiredDurationSeconds": number; "passScore": number; "attemptTimeLimitSeconds": number; "questions": Array<{ "type": "SINGLE" | "MULTIPLE" | "TRUE_FALSE"; "prompt": string; "options": Array<QuestionOption>; "correctOptionCodes": Array<string>; "questionCode": string }> };

export type TrainingCourseUpdate = { "title": string; "description"?: string; "mediaFileId": string; "requiredDurationSeconds": number; "passScore": number; "attemptTimeLimitSeconds": number; "questions": Array<{ "type": "SINGLE" | "MULTIPLE" | "TRUE_FALSE"; "prompt": string; "options": Array<QuestionOption>; "correctOptionCodes": Array<string>; "questionCode": string }>; "expectedVersion": number };

export type TrainingCourseAdmin = { "title": string; "description"?: string; "mediaFileId"?: string; "requiredDurationSeconds"?: number; "passScore"?: number; "attemptTimeLimitSeconds"?: number; "questions"?: Array<{ "type": "SINGLE" | "MULTIPLE" | "TRUE_FALSE"; "prompt": string; "options": Array<QuestionOption>; "correctOptionCodes": Array<string>; "questionCode": string }>; "trainingId": string; "courseVersion": number; "status": "DRAFT" | "PUBLISHED" | "DISABLED"; "version": number };

export type FaceSessionStatus = "CREATED" | "PROCESSING" | "PASSED" | "FAILED" | "EXPIRED";

export type FaceSessionCreate = { "applicationId": string; "expectedVersion": number; "consentVersion": string };

export type FaceSession = { "verificationSessionId": string; "applicationId": string; "status": FaceSessionStatus; "launchMode": "WEBVIEW_URL"; "launchUrl": (string) | null; "expiresAt": string; "resultAvailable": boolean; "failureCode": (string) | null; "version": number };

export type OrderContactRequest = { "target": "OWNER" | "DRIVER" | "STATION"; "purpose": string; "expectedVersion": number };

export type OrderContactAccess = { "mode": "VIRTUAL_NUMBER" | "SERVICE_NUMBER"; "dialNumber": string; "expiresAt": string; "auditRef": string };

export type PublicStationView = { "stationId": string; "name": string; "address": string; "location"?: GeoPoint; "phoneMask"?: string; "supportedVehicleTypeCodes"?: Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number };

export type OrderTimelinePublic = { "eventId": string; "orderNo": string; "action": string; "displayText": string; "actorLabel": string; "at": string };

export type OccupationType = "NONE" | "INVITE" | "ASSIGNMENT";

export type PrivacyCaseStatus = "APPLIED" | "PROCESSING" | "COMPLETED" | "REJECTED";

export type ExportTaskStatus = "APPLIED" | "APPROVED" | "REJECTED" | "PROCESSING" | "READY" | "FAILED" | "EXPIRED";

export type SensitiveAccessStatus = "APPLIED" | "APPROVED" | "REJECTED" | "EXPIRED" | "REVOKED";

export type ReconcileStatus = "OPEN" | "CHECKING" | "RESOLVED";

export type FieldViolation = { "field": string; "reasonCode": string; "message": string };

export type ConflictItem = { "resourceType": string; "resourceId": string; "reasonCode": string; "currentVersion": (number) | null };

export type ApiErrorDetails = { "fieldErrors"?: Array<FieldViolation>; "conflicts"?: Array<ConflictItem>; "missingPhotoTypes"?: Array<PhotoType>; "remainingAttempts"?: (number) | null; "currentVersion"?: (number) | null };

export type ApiError = { "code": 400 | 401 | 403 | 404 | 409 | 422 | 429 | 500 | 503; "errorCode": "VI_REQUEST_INVALID" | "VI_AUTH_REQUIRED" | "VI_TOKEN_REALM_MISMATCH" | "VI_REFRESH_REUSED" | "VI_FORBIDDEN" | "VI_SELF_REVIEW_DENIED" | "VI_RESOURCE_NOT_VISIBLE" | "VI_VERSION_CONFLICT" | "VI_STATE_CONFLICT" | "VI_IDEMPOTENCY_CONFLICT" | "VI_SELECTION_CONFLICT" | "VI_CONTEXT_CHANGED" | "VI_SNAPSHOT_CHANGED" | "VI_DATA_INVALID" | "VI_EVIDENCE_INCOMPLETE" | "VI_HANDOVER_CODE_INVALID" | "VI_HANDOVER_LOCKED" | "VI_UNPRICED_LINES" | "VI_REFUND_LIMIT" | "VI_PAYMENT_LIMIT" | "VI_SERVICE_UNAVAILABLE" | "VI_CAPABILITY_NOT_CONFIGURED" | "VI_TRAINING_NOT_READY" | "VI_RATE_LIMITED" | "VI_INTERNAL_ERROR" | "VI_AUTHORITY_UNAVAILABLE" | "VI_UPSTREAM_UNAVAILABLE" | "VI_PHONE_PROOF_REQUIRED" | "VI_PHONE_PROOF_REUSED" | "VI_EMPLOYEE_LINK_CONFLICT" | "VI_EMPLOYEE_LINK_BLOCKED"; "msg": string; "requestId": string; "retryable": boolean; "retryAfterSeconds": (number) | null; "data": ApiErrorDetails };

export type PermissionCode = "biz:aftersale:create" | "biz:aftersale:list" | "biz:aftersale:query" | "biz:aftersale:review" | "biz:aftersale:reviewpreview" | "biz:assignment:list" | "biz:audit:list" | "biz:capacity:edit" | "biz:capacity:list" | "biz:config:edit" | "biz:config:query" | "biz:dashboard:list" | "biz:dispatch:create" | "biz:dispatch:edit" | "biz:dispatch:list" | "biz:dispatch:preview" | "biz:dispatch:query" | "biz:dispute:list" | "biz:dispute:query" | "biz:dispute:resolve" | "biz:driver:disablenewassignments" | "biz:driver:edit" | "biz:driver:list" | "biz:driver:query" | "biz:driveradjust:audit" | "biz:driveradjust:create" | "biz:driveradjust:list" | "biz:driverfeerule:create" | "biz:driverfeerule:edit" | "biz:driverfeerule:list" | "biz:driverfeerule:publish" | "biz:driverfeerule:query" | "biz:driverfeerule:trial" | "biz:driverledger:list" | "biz:driverpayment:create" | "biz:driverpayment:list" | "biz:driverpayment:query" | "biz:driverpayment:verify" | "biz:driverqualification:audit" | "biz:driverqualification:list" | "biz:driverqualification:query" | "biz:driverstatement:add" | "biz:driverstatement:create" | "biz:driverstatement:list" | "biz:driverstatement:preview" | "biz:driverstatement:price" | "biz:driverstatement:query" | "biz:driverstatement:remove" | "biz:driverstatement:review" | "biz:driverstatement:send" | "biz:driverstatement:submitreview" | "biz:driverstatement:void" | "biz:driverstatement:withdraw" | "biz:employeebinding:list" | "biz:employeebinding:query" | "biz:employeebinding:rematch" | "biz:employeebinding:revoke" | "biz:employeebinding:suspend" | "biz:exception:list" | "biz:exception:resolve" | "biz:export:audit" | "biz:export:query" | "biz:files:access" | "biz:files:query" | "biz:handover:confirm" | "biz:handover:create" | "biz:handover:query" | "biz:handover:secret" | "biz:job:list" | "biz:job:retry" | "biz:license:edit" | "biz:license:list" | "biz:notificationtemplate:edit" | "biz:notificationtemplate:list" | "biz:order:contact" | "biz:order:create" | "biz:order:export" | "biz:order:list" | "biz:order:query" | "biz:owner:edit" | "biz:owner:list" | "biz:owner:query" | "biz:paymentreconciliation:list" | "biz:paymentreconciliation:query" | "biz:paymentreconciliation:resolve" | "biz:penalty:list" | "biz:penalty:query" | "biz:penaltyappeal:audit" | "biz:penaltyappeal:list" | "biz:penaltyappeal:query" | "biz:price:audit" | "biz:price:create" | "biz:price:edit" | "biz:price:list" | "biz:price:publish" | "biz:price:submit" | "biz:privacycase:list" | "biz:privacycase:resolve" | "biz:refund:query" | "biz:refund:retry" | "biz:result:create" | "biz:result:list" | "biz:resultcorrection:audit" | "biz:resultcorrection:create" | "biz:resultcorrection:list" | "biz:statementdifference:list" | "biz:statementdifference:resolve" | "biz:station:create" | "biz:station:edit" | "biz:station:list" | "biz:station:query" | "biz:stationgrant:edit" | "biz:stationgrant:list" | "biz:statistics:list" | "biz:trainingcourse:create" | "biz:trainingcourse:edit" | "biz:trainingcourse:list" | "biz:trainingcourse:publish" | "biz:trainingcourse:query" | "biz:uploads:complete" | "biz:uploads:create" | "biz:vehicle:edit" | "biz:vehicle:list" | "biz:vehicle:query";

export type CapabilityCode = "COMMON_DICT_QUERY" | "COMMON_FILES_ACCESS" | "COMMON_FILES_QUERY" | "COMMON_MESSAGES_LIST" | "COMMON_MESSAGES_READ" | "COMMON_ORDERS_CONFIRM" | "COMMON_ORDERS_CONTACTACCESS" | "COMMON_ORDERS_CREATE" | "COMMON_ORDERS_LIST" | "COMMON_ORDERS_QUERY" | "COMMON_PHONE_CREATE" | "COMMON_PHONE_SEND" | "COMMON_PHONE_VERIFY" | "COMMON_PRIVACYCASES_CREATE" | "COMMON_PRIVACYCASES_LIST" | "COMMON_STATIONS_LIST" | "COMMON_UPLOADS_COMPLETE" | "COMMON_UPLOADS_CREATE" | "DRIVER_ASSIGNMENT_QUERY" | "DRIVER_AVAILABILITY_EDIT" | "DRIVER_AVAILABILITY_QUERY" | "DRIVER_DRIVERPAYMENT_LIST" | "DRIVER_DRIVERSTATEMENT_CONFIRM" | "DRIVER_DRIVERSTATEMENT_CREATE" | "DRIVER_DRIVERSTATEMENT_LIST" | "DRIVER_DRIVERSTATEMENT_QUERY" | "DRIVER_INVITE_LIST" | "DRIVER_INVITE_QUERY" | "DRIVER_INVITE_RESPONSE" | "DRIVER_LOCATION_CREATE" | "DRIVER_ORDER_ARRIVEPICKUP" | "DRIVER_ORDER_ARRIVESTATION" | "DRIVER_ORDER_ASSIGNMENTCANCELLATION" | "DRIVER_ORDER_CREATE" | "DRIVER_ORDER_DEPART" | "DRIVER_ORDER_EDIT" | "DRIVER_ORDER_LIST" | "DRIVER_ORDER_QUERY" | "DRIVER_ORDER_READY" | "DRIVER_PENALTYAPPEAL_LIST" | "DRIVER_PENALTYAPPEAL_QUERY" | "DRIVER_PENALTY_CREATE" | "DRIVER_PENALTY_LIST" | "DRIVER_PENALTY_QUERY" | "DRIVER_QUALIFICATION_ATTEMPTS" | "DRIVER_QUALIFICATION_CREATE" | "DRIVER_QUALIFICATION_EDIT" | "DRIVER_QUALIFICATION_LIST" | "DRIVER_QUALIFICATION_PLAYBACK" | "DRIVER_QUALIFICATION_PROGRESS" | "DRIVER_QUALIFICATION_QUERY" | "DRIVER_QUALIFICATION_SUBMIT" | "EMPLOYEE_LINK_READ" | "EMPLOYEE_LINK_RECONCILE" | "OWNER_ADDRESSES_CREATE" | "OWNER_ADDRESSES_DEFAULT" | "OWNER_ADDRESSES_EDIT" | "OWNER_ADDRESSES_LIST" | "OWNER_ADDRESSES_REMOVE" | "OWNER_AFTERSALE_CREATE" | "OWNER_AFTERSALE_LIST" | "OWNER_AFTERSALE_QUERY" | "OWNER_ORDER_CLOSEUNPAID" | "OWNER_ORDER_CREATE" | "OWNER_ORDER_LIST" | "OWNER_ORDER_QUERY" | "OWNER_ORDER_SERVICECONFIRMATION" | "OWNER_PROFILE_EDIT" | "OWNER_SCHEDULE_LIST" | "OWNER_VEHICLE_CREATE" | "OWNER_VEHICLE_DEFAULT" | "OWNER_VEHICLE_EDIT" | "OWNER_VEHICLE_LIST" | "OWNER_VEHICLE_REMOVE" | "PHONE_BIND" | "PROFILE_READ" | "SESSION_LOGOUT" | "SESSION_READ" | "SESSION_SWITCH_ROLE" | "SESSION_SWITCH_STATION";

export type AllowedAction = "ADD_REVIEW" | "ADD_TO_DRAFT" | "APPEAL_PENALTY" | "APPLY_ADJUSTMENT" | "APPLY_CANCEL_REFUND" | "APPLY_REFUND_ONLY" | "APPROVE_AND_SEND" | "ARRIVE_PICKUP" | "ARRIVE_STATION" | "CANCEL_ASSIGNMENT" | "CLOSE_UNPAID" | "CONFIRM_HANDOVER" | "CONFIRM_STATEMENT" | "CONTACT_DRIVER" | "CONTACT_OWNER" | "CONTACT_STATION" | "CREATE_HANDOVER" | "DEPART" | "EDIT_PRICE" | "EXPORT" | "FILL_MILEAGE" | "FILL_PRICE" | "MANUAL_INVITE" | "MARK_EVIDENCE_READY" | "OPEN_DISPUTE" | "PAY" | "RECORD_PAYMENT" | "REGISTER_RESULT" | "REJECT_REVIEW" | "REMOVE_FROM_DRAFT" | "RENOTIFY_PICKUP" | "REPORT_DIFFERENCE" | "REPORT_EXCEPTION" | "REQUEST_RESULT_CORRECTION" | "REVIEW_AFTERSALE" | "REVIEW_LICENSE" | "SEND_STATEMENT" | "SERVICE_CONFIRM" | "SUBMIT_REVIEW" | "UPLOAD_PHOTO" | "VERIFY_PAYMENT" | "VIEW_APPEAL" | "VIEW_DETAIL" | "VIEW_EVIDENCE" | "VIEW_HANDOVER_SECRET" | "VIEW_PAYMENT" | "VIEW_PRICE_HISTORY" | "VIEW_PRICING" | "VIEW_RESULT" | "VOID_STATEMENT" | "WITHDRAW_STATEMENT";

export type AccountStatus = "ACTIVE" | "LOCKED";

export type VerificationType = "REALNAME" | "FACE" | "BACKGROUND" | "LICENSE" | "TRAINING";

export type VerificationStatus = "CREATED" | "PROCESSING" | "PASSED" | "FAILED" | "EXPIRED" | "UNAVAILABLE";

export type VehicleVerificationStatus = "PENDING" | "VERIFIED" | "REJECTED";

export type PriceRuleStatus = "DRAFT" | "IN_REVIEW" | "APPROVED" | "PUBLISHED" | "DISABLED";

export type QuotaType = "PICKUP" | "STATION_RECEIVE";

export type ReservationStatus = "HELD" | "CONFIRMED" | "CONSUMED" | "RELEASED";

export type AssignmentEndReason = "RETURNED" | "CANCELLED" | "DRIVER_CANCEL";

export type OccupancyHolderType = "INVITE" | "ASSIGNMENT";

export type EvidenceStatus = "DRAFT" | "READY" | "SEALED";

export type FileStatus = "UPLOADING" | "VERIFYING" | "READY" | "REJECTED";

export type LicenseReviewStatus = "MISSING" | "PENDING" | "VERIFIED";

export type CorrectionStatus = "APPLIED" | "APPROVED" | "REJECTED";

export type ChannelRefundStatus = "CREATED" | "PROCESSING" | "SUCCESS" | "CLOSED" | "ABNORMAL" | "UNKNOWN";

export type ServiceCaseStatus = "OPEN" | "PROCESSING" | "RESOLVED";

export type OutboxStatus = "READY" | "PROCESSING" | "DONE" | "FAILED";

export type NotificationSendStatus = "PENDING" | "ACCEPTED" | "FAILED" | "DISCARDED";

export type IdempotencyStatus = "PROCESSING" | "DONE";

export type GovernanceType = "PRIVACY" | "EXPORT" | "SENSITIVE_ACCESS" | "RECONCILE";

export type OwnerServiceFlag = "NORMAL" | "VERIFY_AGAIN";

export type AfterSaleStatus = "PENDING_REVIEW" | "EXECUTING" | "CLOSED" | "REJECTED";

export type AfterSaleReviewStatus = "PENDING" | "APPROVED" | "REJECTED";

export type ApplicantType = "ACCOUNT" | "SYS_USER";

export type DriverFeeRuleStatus = "DRAFT" | "PENDING_REVIEW" | "PUBLISHED" | "DISABLED";

export type LedgerPricingStatus = "PENDING_PRICING" | "READY";

export type StatementStatus = "DRAFT" | "INTERNAL_REVIEW" | "SENT" | "DISPUTED" | "CONFIRMED" | "VOID";

export type StatementReviewStatus = "NOT_REQUIRED" | "REQUIRED" | "PENDING" | "APPROVED" | "REJECTED";

export type SelectionMode = "EXPLICIT";

export type PricingSource = "RULE" | "UNPRICED" | "MANUAL_FILLED" | "MANUAL_CHANGED" | "APPROVED_ADJUSTMENT";

export type StatementDifferenceStatus = "OPEN" | "RESOLVED";

export type AdjustmentStatus = "PENDING" | "APPROVED" | "REJECTED";

export type ExternalPaymentStatus = "PENDING" | "VERIFIED" | "REJECTED" | "REVERSED";

export type SessionStatus = "ACTIVE" | "REVOKED" | "EXPIRED";

export type SessionTokenType = "ACCESS" | "REFRESH";

export type InternalReviewRecordStatus = "PENDING" | "APPROVED" | "REJECTED" | "WITHDRAWN";

export type TrainingCourseStatus = "DRAFT" | "PUBLISHED" | "DISABLED";

export type QuestionType = "SINGLE" | "MULTIPLE" | "TRUE_FALSE";

export type FaceLaunchMode = "WEBVIEW_URL";

export type GovernanceStatus = "APPLIED" | "APPROVED" | "CHECKING" | "COMPLETED" | "EXPIRED" | "FAILED" | "OPEN" | "PROCESSING" | "READY" | "REJECTED" | "RESOLVED" | "REVOKED";

export type GeoPoint = { "lng": number; "lat": number; "coordinateSystem": "GCJ02" };

export type AppealRecordStatus = "SUBMITTED" | "APPROVED" | "REJECTED";

export type AdminPenaltyView = { "penaltyId": string; "assignmentId": string; "orderNo": string; "type": PenaltyType; "status": PenaltyStatus; "businessDate": string; "reason": string; "ruleVersion": string; "roundKey": (string) | null; "effectiveUntil": (string) | null; "appealStatus": PenaltyAppealStatus; "latestAppealId": (string) | null; "version": number; "allowedActions": Array<AllowedAction>; "driverId": string; "driverDisplayName": string };

export type AdminPenaltyAppealView = { "appealId": string; "penaltyId": string; "status": AppealRecordStatus; "reason": string; "evidenceFileIds": Array<string>; "submittedAt": string; "publicOpinion": (string) | null; "reviewedAt": (string) | null; "version": number; "driverId": string; "driverDisplayName": string };

export type PriceCreate = { "name": string; "vehicleTypeCode": string; "inspectionTypeCode": string; "inspectionFee": number; "driverBaseFee": number; "includedDistanceMeter": number; "distanceUnitMeter": number; "distanceUnitFee": number; "distanceRounding": "CEIL_UNIT" | "EXACT_PRORATA"; "routeBasis": "ONE_WAY" | "ROUND_TRIP"; "waitingFeeEnabled"?: boolean; "waitingFreeSeconds"?: number; "waitingUnitSeconds"?: number; "waitingUnitFee"?: number; "effectiveFrom"?: string; "effectiveTo"?: string };

export type PriceUpdate = { "name": string; "vehicleTypeCode": string; "inspectionTypeCode": string; "inspectionFee": number; "driverBaseFee": number; "includedDistanceMeter": number; "distanceUnitMeter": number; "distanceUnitFee": number; "distanceRounding": "CEIL_UNIT" | "EXACT_PRORATA"; "routeBasis": "ONE_WAY" | "ROUND_TRIP"; "waitingFeeEnabled"?: boolean; "waitingFreeSeconds"?: number; "waitingUnitSeconds"?: number; "waitingUnitFee"?: number; "effectiveFrom"?: string; "effectiveTo"?: string; "expectedVersion": number };

export type DriverFeeRuleCreate = { "name": string; "payorEntityId": string; "currency": "CNY"; "driverScope"?: Array<string>; "vehicleTypeCodes"?: Array<string>; "scenario": "NORMAL_RETURN" | "CANCEL_RETURN" | "CANCEL_BEFORE_PICKUP"; "baseAmount": number; "includedDistanceMeter": number; "unitDistanceMeter": number; "unitAmount": number; "rounding": "CEIL" | "HALF_UP"; "routeBasis": "CONTRACT_ROUTE"; "effectiveFrom": string; "effectiveTo"?: string; "changeReason": string };

export type DriverFeeRuleUpdate = { "expectedVersion": number; "name": string; "payorEntityId": string; "currency": "CNY"; "driverScope"?: Array<string>; "vehicleTypeCodes"?: Array<string>; "scenario": "NORMAL_RETURN" | "CANCEL_RETURN" | "CANCEL_BEFORE_PICKUP"; "baseAmount": number; "includedDistanceMeter": number; "unitDistanceMeter": number; "unitAmount": number; "rounding": "CEIL" | "HALF_UP"; "routeBasis": "CONTRACT_ROUTE"; "effectiveFrom": string; "effectiveTo"?: string; "changeReason": string };

export type DispatchConfigView = { "dispatchLeadMinutes": number; "inviteResponseSeconds"?: number; "radiusMeter"?: number; "expandedRadiusMeter"?: number; "overallDeadlineSeconds"?: number; "roundPolicy"?: string; "locationMaxAgeSeconds"?: number; "dailyCancelLimit"?: number; "version": number };

export type ConfigPatchView = { "items": Array<ConfigItem>; "version": number };

export type DriverStatementLinePublic = { "lineId": string; "entryId": string; "assignmentId": string; "orderNo": string; "plateNo": string; "serviceEndedAt": string; "amount": number; "pricingExplanation": string };

export type OwnerOrderAction = "PAY" | "CLOSE_UNPAID" | "APPLY_CANCEL_REFUND" | "APPLY_REFUND_ONLY" | "SERVICE_CONFIRM" | "OPEN_DISPUTE" | "ADD_REVIEW" | "CONTACT_DRIVER" | "CONTACT_STATION" | "VIEW_DETAIL" | "VIEW_EVIDENCE" | "VIEW_RESULT" | "VIEW_HANDOVER_SECRET";

export type DriverOrderAction = "DEPART" | "ARRIVE_PICKUP" | "ARRIVE_STATION" | "CANCEL_ASSIGNMENT" | "CONTACT_OWNER" | "CONTACT_STATION" | "CREATE_HANDOVER" | "CONFIRM_HANDOVER" | "MARK_EVIDENCE_READY" | "UPLOAD_PHOTO" | "FILL_MILEAGE" | "REPORT_EXCEPTION" | "VIEW_DETAIL" | "VIEW_RESULT" | "VIEW_EVIDENCE" | "VIEW_HANDOVER_SECRET";

export type StationOrderAction = "CONTACT_DRIVER" | "CREATE_HANDOVER" | "CONFIRM_HANDOVER" | "REVIEW_LICENSE" | "REGISTER_RESULT" | "REQUEST_RESULT_CORRECTION" | "RENOTIFY_PICKUP" | "REPORT_EXCEPTION" | "VIEW_DETAIL" | "VIEW_EVIDENCE" | "VIEW_RESULT" | "VIEW_HANDOVER_SECRET";

export type AdminOrderAction = "MANUAL_INVITE" | "REVIEW_AFTERSALE" | "REVIEW_LICENSE" | "REGISTER_RESULT" | "REQUEST_RESULT_CORRECTION" | "CONTACT_OWNER" | "CONTACT_DRIVER" | "CONTACT_STATION" | "EXPORT" | "VIEW_DETAIL" | "VIEW_EVIDENCE" | "VIEW_RESULT" | "VIEW_PAYMENT";

export type PublicServiceConfig = { "version": number; "companyName": string; "supportPhone": string; "termsVersion": string; "termsText": string; "privacyVersion": string; "privacyText": string; "serviceCities": Array<string>; "bookingEnabled": boolean };

export type EmployeeLinkStatus = "UNLINKED" | "LINKED" | "NEEDS_ADMIN" | "SUSPENDED" | "REVOKED";

export type EmployeeBindingLifecycle = "ACTIVE" | "SUSPENDED" | "REVOKED";

export type PhoneVerificationStatus = "PROCESSING" | "VERIFIED" | "UNKNOWN" | "FAILED";

export type EmployeeLinkOutcome = "NOT_ATTEMPTED" | "NO_MATCH" | "LINKED" | "EXISTING_LINK" | "AMBIGUOUS_PHONE" | "EMPLOYEE_UNAVAILABLE" | "USER_ALREADY_LINKED" | "ACCOUNT_ALREADY_LINKED" | "REBIND_BLOCKED" | "PHONE_CHANGED" | "DIRECTORY_UNAVAILABLE";

export type EmployeeLinkPublic = { "status": EmployeeLinkStatus; "workAccessEnabled": boolean; "authorizedStationCount": number; "messageKey": "NO_EMPLOYEE_MATCH" | "EMPLOYEE_LINKED" | "WAIT_ADMIN_CONFIGURATION" | "CONTACT_ADMIN" | "PHONE_REVERIFY_REQUIRED" | "LINK_REVOKED"; "canReverifyPhone": boolean };

export type PhoneBindResult = { "verificationRequestId": string; "verificationStatus": PhoneVerificationStatus; "phoneBound": boolean; "phoneMask": (string) | null; "accountVersion": number; "employeeLink": EmployeeLinkPublic; "requiresSessionRefresh": boolean };

export type EmployeeLinkReconcile = { "expectedContextVersion": number };

export type EmployeeLinkAdmin = { "bindingId": string; "accountId": string; "appId": string; "sysUserId": string; "employeeName": string; "phoneMask": string; "openIdMask": string; "status": EmployeeBindingLifecycle; "source": "WECHAT_PHONE_MATCH"; "boundAt": string; "lastPhoneVerifiedAt": string; "autoRebindBlocked": boolean; "version": number };

export type EmployeeLinkAction = { "expectedVersion": number; "reason": string };

export type EmployeeLinkRecheckResult = { "binding": EmployeeLinkAdmin; "requiresNewPhoneAuthorization": boolean };

export type StationGrantView = { "employeeUserId": string; "ownStationId": (string) | null; "authorizedStationIds": Array<string>; "version": number; "wechatLinkStatus": "NOT_LOGGED_IN_OR_UNLINKED" | "LINKED" | "SUSPENDED" | "REVOKED"; "updatedAt": (string) | null };

export type EmployeeLinkSource = "WECHAT_PHONE_MATCH";

export type PhoneProofSource = "WECHAT" | "SMS";

export type AdminStationCreate = { "deptId": string; "name": string; "address": string; "location": GeoPoint; "contactPhone"?: string; "enabled"?: boolean; "supportedVehicleTypeCodes": Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number; "qualificationFileIds"?: Array<string> };

export type AdminStationUpdate = { "deptId"?: string; "name": string; "address": string; "location": GeoPoint; "contactPhone"?: string; "enabled"?: boolean; "supportedVehicleTypeCodes": Array<string>; "businessHours"?: string; "serviceRadiusMeter"?: number; "qualificationFileIds"?: Array<string>; "expectedVersion": number };

export type CapacityCreate = { "stationId": string; "quotaType": "PICKUP" | "STATION_RECEIVE"; "startAt": string; "endAt": string; "total": number; "reason": string };
