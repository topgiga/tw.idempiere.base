-- Helper Function: tg_get_doc_status
-- Dynamic fetch of docstatus column from document table
CREATE OR REPLACE FUNCTION tg_get_doc_status(p_tablename varchar, p_record_id numeric)
RETURNS varchar AS $$
DECLARE
    v_docstatus varchar;
    v_sql varchar;
BEGIN
    IF p_tablename IS NULL OR p_record_id IS NULL THEN
        RETURN NULL;
    END IF;
    v_sql := format('SELECT docstatus::varchar FROM %I WHERE %I = $1', lower(p_tablename), lower(p_tablename) || '_id');
    BEGIN
        EXECUTE v_sql INTO v_docstatus USING p_record_id;
    EXCEPTION WHEN OTHERS THEN
        v_docstatus := NULL;
    END;
    RETURN v_docstatus;
END;
$$ LANGUAGE plpgsql STABLE;

-- Helper Function: tg_get_doc_date
-- Dynamic fetch of document date from document table
CREATE OR REPLACE FUNCTION tg_get_doc_date(p_tablename varchar, p_record_id numeric)
RETURNS timestamp AS $$
DECLARE
    v_docdate timestamp;
    v_date_col varchar;
    v_sql varchar;
BEGIN
    IF p_tablename IS NULL OR p_record_id IS NULL THEN
        RETURN NULL;
    END IF;

    SELECT column_name INTO v_date_col
    FROM information_schema.columns
    WHERE lower(table_name) = lower(p_tablename)
      AND lower(column_name) IN ('datedoc', 'dateordered', 'dateinvoiced', 'datetrx', 'movementdate', 'dateacct', 'created')
    ORDER BY CASE lower(column_name)
        WHEN 'datedoc' THEN 1
        WHEN 'dateordered' THEN 2
        WHEN 'dateinvoiced' THEN 3
        WHEN 'datetrx' THEN 4
        WHEN 'movementdate' THEN 5
        WHEN 'dateacct' THEN 6
        WHEN 'created' THEN 7
        ELSE 8
    END
    LIMIT 1;

    IF v_date_col IS NOT NULL THEN
        v_sql := format('SELECT %I::timestamp FROM %I WHERE %I = $1', v_date_col, lower(p_tablename), lower(p_tablename) || '_id');
        BEGIN
            EXECUTE v_sql INTO v_docdate USING p_record_id;
        EXCEPTION WHEN OTHERS THEN
            v_docdate := NULL;
        END;
    END IF;

    RETURN v_docdate;
END;
$$ LANGUAGE plpgsql STABLE;

-- Helper Function: tg_get_doctype_id
-- Dynamic fetch of c_doctype_id from document table
CREATE OR REPLACE FUNCTION tg_get_doctype_id(p_tablename varchar, p_record_id numeric)
RETURNS numeric AS $$
DECLARE
    v_doctype_id numeric;
    v_col varchar;
    v_sql varchar;
BEGIN
    IF p_tablename IS NULL OR p_record_id IS NULL THEN
        RETURN NULL;
    END IF;

    SELECT column_name INTO v_col
    FROM information_schema.columns
    WHERE lower(table_name) = lower(p_tablename)
      AND lower(column_name) IN ('c_doctype_id', 'c_doctypetarget_id')
    ORDER BY CASE lower(column_name)
        WHEN 'c_doctype_id' THEN 1
        WHEN 'c_doctypetarget_id' THEN 2
        ELSE 3
    END
    LIMIT 1;

    IF v_col IS NOT NULL THEN
        v_sql := format('SELECT %I FROM %I WHERE %I = $1', v_col, lower(p_tablename), lower(p_tablename) || '_id');
        BEGIN
            EXECUTE v_sql INTO v_doctype_id USING p_record_id;
        EXCEPTION WHEN OTHERS THEN
            v_doctype_id := NULL;
        END;
    END IF;

    RETURN v_doctype_id;
END;
$$ LANGUAGE plpgsql STABLE;

-- View: rv_wf_activity_check
-- Query AD_WF_Activity records where Suspended > 30 days OR Document Status IS NOT 'IP'
CREATE OR REPLACE VIEW rv_wf_activity_check AS
WITH base AS (
    SELECT 
        a.ad_wf_activity_id AS rv_wf_activity_check_id,
        a.ad_wf_activity_id,
        a.ad_client_id,
        a.ad_org_id,
        a.isactive,
        a.created,
        a.createdby,
        a.updated,
        a.updatedby,
        a.ad_wf_process_id,
        a.ad_wf_node_id,
        a.ad_user_id,
        a.ad_wf_responsible_id,
        a.wfstate,
        a.processed,
        a.ad_workflow_id,
        a.ad_table_id,
        t.tablename,
        a.record_id,
        a.textmsg,
        tg_get_doc_status(t.tablename, a.record_id) AS docstatus,
        tg_get_doctype_id(t.tablename, a.record_id) AS c_doctype_id,
        tg_get_doc_date(t.tablename, a.record_id) AS docdate
    FROM ad_wf_activity a
    JOIN ad_table t ON a.ad_table_id = t.ad_table_id
    WHERE a.processed = 'N' 
      AND a.isactive = 'Y'
)
SELECT 
    b.rv_wf_activity_check_id,
    b.ad_wf_activity_id,
    b.ad_client_id,
    b.ad_org_id,
    b.isactive,
    b.created,
    b.createdby,
    b.updated,
    b.updatedby,
    b.ad_wf_process_id,
    b.ad_wf_node_id,
    b.ad_user_id,
    b.ad_wf_responsible_id,
    b.wfstate,
    b.processed,
    b.ad_workflow_id,
    b.ad_table_id,
    b.tablename,
    b.record_id,
    b.c_doctype_id,
    dt.name AS doctypename,
    b.docdate,
    b.docstatus,
    b.textmsg
FROM base b
LEFT JOIN c_doctype dt ON dt.c_doctype_id = b.c_doctype_id
WHERE (b.wfstate = 'OS' AND b.updated <= NOW() - INTERVAL '30 days')
   OR (b.docstatus IS NULL OR b.docstatus <> 'IP');
