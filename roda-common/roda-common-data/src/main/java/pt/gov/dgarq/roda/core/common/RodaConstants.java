package pt.gov.dgarq.roda.core.common;

public final class RodaConstants {

	/*
	 * Misc
	 */
	public final static String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public final static String SHA1 = "SHA-1";
	public final static String MD5 = "MD5";

	/*
	 * INDEX NAMES
	 */
	public static final String INDEX_AIP = "AIP";
	public static final String INDEX_SDO = "SDO";
	// FIXME not in use. is this to remove?
	public static final String INDEX_DESCRIPTIVE_METADATA = "DescriptiveMetadata";
	public static final String INDEX_PRESERVATION_EVENTS = "PreservationEvent";
	public static final String INDEX_PRESERVATION_OBJECTS = "PreservationObject";
	public static final String INDEX_REPRESENTATIONS = "Representation";
	public static final String INDEX_NOTIFICATIONS = "Notification";
	public static final String INDEX_PRESERVATION_AGENTS = "PreservationAgent";
	public static final String INDEX_PRESERVATION_PLANS = "PreservationPlan";
	public static final String INDEX_PRESERVATION_RISKS = "PreservationRisk";
	public static final String INDEX_USER_LOG = "UserLog";
	public static final String INDEX_ACTION_LOG = "ActionLog";
	public static final String INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX = "odd";
	public static final String INDEX_SIP_REPORT = "SIPReport";

	/*
	 * STORAGE CONTAINERS
	 */
	public static final String STORAGE_CONTAINER_SIP = "SIP";
	public static final String STORAGE_CONTAINER_AIP = "AIP";
	public static final String STORAGE_CONTAINER_NOTIFICATIONS = "Notifications";
	public static final String STORAGE_CONTAINER_PRESERVATION = "Preservation";
	public static final String STORAGE_CONTAINER_USERLOG = "User log";
	public static final String STORAGE_CONTAINER_ACTIONLOG = "Action log";
	public static final String STORAGE_CONTAINER_SIP_REPORT = "SIP report";

	/*
	 * STORAGE DIRECTORIES
	 */
	public static final String STORAGE_DIRECTORY_METADATA = "metadata";
	public static final String STORAGE_DIRECTORY_DESCRIPTIVE = "descriptive";
	public static final String STORAGE_DIRECTORY_PRESERVATION = "preservation";
	public static final String STORAGE_DIRECTORY_REPRESENTATION_PREFIX = "representation_";
	public static final String STORAGE_DIRECTORY_DATA = "data";
	public static final String STORAGE_DIRECTORY_AGENTS = "agents";

	/*
	 * STORAGE METADATA
	 */
	public static final String STORAGE_META_PARENT_ID = "parentId";
	public static final String STORAGE_META_TYPE = "type";
	public static final String STORAGE_META_ACTIVE = "active";
	public static final String STORAGE_META_ENTRYPOINT = "entryPoint";
	public static final String STORAGE_META_FORMAT_MIME = "format.mimetype";
	public static final String STORAGE_META_FORMAT_VERSION = "format.version";
	public static final String STORAGE_META_DATE_CREATED = "date.created";
	public static final String STORAGE_META_DATE_MODIFIED = "date.modified";
	public static final String STORAGE_META_AIP_ID = "aip.id";
	public static final String STORAGE_META_REPRESENTATION_ID = "representation.id";
	public static final String STORAGE_META_REPRESENTATION_STATUSES = "representation.statuses";
	public static final String STORAGE_META_DIGEST_SHA1 = "digest.sha1";
	public static final String STORAGE_META_PERMISSION_GRANT_USERS = "permission.grant.users";
	public static final String STORAGE_META_PERMISSION_GRANT_GROUPS = "permission.grant.groups";
	public static final String STORAGE_META_PERMISSION_READ_USERS = "permission.read.users";
	public static final String STORAGE_META_PERMISSION_READ_GROUPS = "permission.read.groups";
	// XXX the following two constants formerly were known as producers
	// permissions
	public static final String STORAGE_META_PERMISSION_INSERT_USERS = "permission.insert.users";
	public static final String STORAGE_META_PERMISSION_INSERT_GROUPS = "permission.insert.groups";
	public static final String STORAGE_META_PERMISSION_MODIFY_USERS = "permission.modify.users";
	public static final String STORAGE_META_PERMISSION_MODIFY_GROUPS = "permission.modify.groups";
	public static final String STORAGE_META_PERMISSION_REMOVE_USERS = "permission.remove.users";
	public static final String STORAGE_META_PERMISSION_REMOVE_GROUPS = "permission.remove.groups";

	/*
	 * SIP FIELDS
	 */
	public static final String SIP_NAME = "name";
	public static final String SIP_SUBMISSION_DATE = "submissionDate";
	public static final String SIP_STATE_CURRENT = "state";
	public static final String SIP_PERCENTAGE = "percentage";
	public static final String SIP_PRODUCER = "producer";
	public static final String SIP_CREATED_AIP = "createdAIPs";

	/*
	 * AIP FIELDS
	 */
	public static final String AIP_ID = "id";
	public static final String AIP_PARENT_ID = "parentId";
	public static final String AIP_ACTIVE = "active";

	public static final String AIP_DATE_CREATED = "dateCreated";
	public static final String AIP_DATE_MODIFIED = "dateModified";
	public static final String AIP_DESCRIPTIVE_METADATA_ID = "descriptiveMetadataId";
	public static final String AIP_REPRESENTATION_ID = "representationId";
	public static final String AIP_HAS_REPRESENTATIONS = "hasRepresentations";
	public static final String AIP_PRESERVATION_OBJECTS_ID = "preservationObjectsId";
	public static final String AIP_PRESERVATION_EVENTS_ID = "preservationEventsId";

	public static final String AIP_PERMISSION_GRANT_USERS = STORAGE_META_PERMISSION_GRANT_USERS;
	public static final String AIP_PERMISSION_GRANT_GROUPS = STORAGE_META_PERMISSION_GRANT_GROUPS;
	public static final String AIP_PERMISSION_READ_USERS = STORAGE_META_PERMISSION_READ_USERS;
	public static final String AIP_PERMISSION_READ_GROUPS = STORAGE_META_PERMISSION_READ_GROUPS;
	// XXX the following two constants formerly were known as producers
	// permissions
	public static final String AIP_PERMISSION_INSERT_USERS = STORAGE_META_PERMISSION_INSERT_USERS;
	public static final String AIP_PERMISSION_INSERT_GROUPS = STORAGE_META_PERMISSION_INSERT_GROUPS;
	public static final String AIP_PERMISSION_MODIFY_USERS = STORAGE_META_PERMISSION_MODIFY_USERS;
	public static final String AIP_PERMISSION_MODIFY_GROUPS = STORAGE_META_PERMISSION_MODIFY_GROUPS;
	public static final String AIP_PERMISSION_REMOVE_USERS = STORAGE_META_PERMISSION_REMOVE_USERS;
	public static final String AIP_PERMISSION_REMOVE_GROUPS = STORAGE_META_PERMISSION_REMOVE_GROUPS;

	/*
	 * SDO FIELDS
	 */
	public static final String SDO_LEVEL = "level";
	public static final String SDO_TITLE = "title";
	public static final String SDO_DATE_INITIAL = "dateInitial";
	public static final String SDO_DATE_FINAL = "dateFinal";
	public static final String SDO_CHILDREN_COUNT = "childrenCount";
	public static final String SDO_DESCRIPTION = "description";
	public static final String SDO_STATE = "state";
	public static final String SDO_LABEL = "label";
	public static final String SDO__ALL = "_all";

	/**
	 * Descriptive Metadata fields
	 */
	public static final String DESCRIPTIVE_METADATA_ID = "id";

	/*
	 * SRO FIELDS
	 */
	public static final String SRO_UUID = "uuid";
	public static final String SRO_ID = "id";
	public static final String SRO_AIP_ID = "aipId";
	public static final String SRO_ACTIVE = "active";
	public static final String SRO_LABEL = "label";
	public static final String SRO_TYPE = "type";
	public static final String SRO_SUBTYPE = "subtype";
	public static final String SRO_DATE_MODIFICATION = "dateModified";
	public static final String SRO_DATE_CREATION = "dateCreated";
	public static final String SRO_STATUS = "status";
	public static final String SRO_FILE_IDS = "fileId";
	public static final String SRO_EVENT_PRESERVATION_IDS = "eventId";
	public static final String SRO_REPRESENTATION_PRESERVATION_IDS = "representationId";

	/*
	 * SEPM FIELDS
	 */
	public static final String SEPM_AGENT_ID = "agentId";
	public static final String SEPM_CREATED_DATE = "dateCreated";
	public static final String SEPM_ID = "id";
	public static final String SEPM_LABEL = "label";
	public static final String SEPM_LAST_MODIFIED_DATE = "dateModified";
	public static final String SEPM_STATE = "state";
	public static final String SEPM_TARGET_ID = "targetId";
	public static final String SEPM_TYPE = "type";
	public static final String SEPM_AIP_ID = "aipId";
	public static final String SEPM_DATETIME = "datetime";
	public static final String SEPM_NAME = "name";
	public static final String SEPM_DESCRIPTION = "description";
	public static final String SEPM_OUTCOME_RESULT = "outcomeResult";
	public static final String SEPM_OUTCOME_DETAILS = "outcomeDetails";
	public static final String SEPM_REPRESENTATION_ID = "representationId";
	public static final String SEPM_FILE_ID = "fileId";

	/*
	 * SRPM FIELDS
	 */
	public static final String SRPM_CREATED_DATE = "dateCreated";
	public static final String SRPM_ID = "id";
	public static final String SRPM_LABEL = "label";
	public static final String SRPM_LAST_MODIFIED_DATE = "dateModified";
	public static final String SRPM_REPRESENTATION_OBJECT_ID = "representationObjectId";
	public static final String SRPM_STATE = "state";
	public static final String SRPM_TYPE = "type";
	public static final String SRPM_AIP_ID = "aipId";
	public static final String SRPM_MODEL = "model";
	public static final String SRPM_REPRESENTATION_ID = "representationId";
	public static final String SRPM_FILE_ID = "fileId";

	/*
	 * SRFM
	 */
	public static final String SRFM_CREATED_DATE = "dateCreated";
	public static final String SRFM_ID = "id";
	public static final String SRFM_LABEL = "label";
	public static final String SRFM_LAST_MODIFIED_DATE = "dateModified";
	public static final String SRFM_REPRESENTATION_OBJECT_ID = "representationObjectId";
	public static final String SRFM_STATE = "state";
	public static final String SRFM_TYPE = "type";
	public static final String SRFM_AIP_ID = "aipId";
	public static final String SRFM_REPRESENTATION_ID = "representationId";
	public static final String SRFM_FILE_ID = "fileId";
	public static final String SRFM_HASH = "hash";
	public static final String SRFM_MIMETYPE = "mimetype";
	public static final String SRFM_PRONOM_ID = "pronomId";
	public static final String SRFM_SIZE = "size";

	/*
	 * OTHER FIELDS
	 */
	public static final String OBJECT_PERMISSIONS_PRODUCERS_USERS = "producersUsers";
	public static final String OBJECT_PERMISSIONS_PRODUCERS_GROUPS = "producersGroups";
	public static final String OBJECT_PERMISSIONS_MODIFY_USERS = "modifyUsers";
	public static final String OBJECT_PERMISSIONS_MODIFY_GROUPS = "modifyGroups";
	public static final String OBJECT_PERMISSIONS_REMOVE_USERS = "removeUsers";
	public static final String OBJECT_PERMISSIONS_REMOVE_GROUPS = "removeGroups";
	public static final String OBJECT_PERMISSIONS_GRANT_USERS = "grantUsers";
	public static final String OBJECT_PERMISSIONS_GRANT_GROUPS = "grantGroups";

	public static final String REPOSITORY_PERMISSIONS_METADATA_EDITOR = "administration.metadata_editor";
	public static final String LOG_ACTION_COMPONENT = "actionComponent";
	public static final String LOG_ACTION_METHOD = "actionMethod";
	public static final String LOG_ADDRESS = "address";
	public static final String LOG_DATETIME = "datetime";
	public static final String LOG_DURATION = "duration";
	public static final String LOG_ID = "id";
	public static final String LOG_RELATED_OBJECT_ID = "relatedObject";
	public static final String LOG_USERNAME = "username";
	public static final String LOG_PARAMETERS = "parameters";
	public static final String LOG_FILE_ID = "fileID";
	
	public static final String SIP_REPORT_ORIGINAL_FILENAME = "originalFilename";
	public static final String SIP_REPORT_ID = "id";
	public static final String SIP_REPORT_USERNAME = "username";
	public static final String SIP_REPORT_STATE = "state";
	public static final String SIP_REPORT_DATETIME = "datetime";
	public static final String SIP_REPORT_PROCESSING = "processing";
	public static final String SIP_REPORT_COMPLETE = "complete";
	public static final String SIP_REPORT_INGESTED_PID = "ingestedPID";
	public static final String SIP_REPORT_COMPLETE_PERCENTAGE = "completePercentage";
	public static final String SIP_REPORT_FILE_ID = "fileID";
	public static final String SIP_REPORT_PARENT_PID = "parentPID";
	public static final String SIP_REPORT_TRANSITION_DATETIME = "transition_datetime";
	public static final String SIP_REPORT_TRANSITION_DESCRIPTION = "transition_description";
	public static final String SIP_REPORT_TRANSITION_FROM = "transition_from";
	public static final String SIP_REPORT_TRANSITION_SIPID = "transition_sipID";
	public static final String SIP_REPORT_TRANSITION_TASKID = "transition_taskID";
	public static final String SIP_REPORT_TRANSITION_TO = "transition_to";
	public static final String SIP_REPORT_TRANSITION_SUCCESS = "transition_success";
	public static final String SIP_REPORT_TRANSITION_ID = "id";
	
	

}
