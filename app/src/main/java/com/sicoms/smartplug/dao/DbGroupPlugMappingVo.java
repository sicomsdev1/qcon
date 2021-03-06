package com.sicoms.smartplug.dao;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table tb_group_plug_mapping.
 */
public class DbGroupPlugMappingVo {

    private Long id;
    private long groupId;
    /** Not-null value. */
    private String plugId;
    private float wh;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient DbGroupPlugMappingVoDao myDao;

    private DbGroupVo dbGroupVo;
    private Long dbGroupVo__resolvedKey;

    private DbPlugVo dbPlugVo;
    private String dbPlugVo__resolvedKey;


    public DbGroupPlugMappingVo() {
    }

    public DbGroupPlugMappingVo(Long id) {
        this.id = id;
    }

    public DbGroupPlugMappingVo(Long id, long groupId, String plugId, float wh) {
        this.id = id;
        this.groupId = groupId;
        this.plugId = plugId;
        this.wh = wh;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDbGroupPlugMappingVoDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    /** Not-null value. */
    public String getPlugId() {
        return plugId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public float getWh() {
        return wh;
    }

    public void setWh(float wh) {
        this.wh = wh;
    }

    /** To-one relationship, resolved on first access. */
    public DbGroupVo getDbGroupVo() {
        long __key = this.groupId;
        if (dbGroupVo__resolvedKey == null || !dbGroupVo__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DbGroupVoDao targetDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVoNew = targetDao.load(__key);
            synchronized (this) {
                dbGroupVo = dbGroupVoNew;
            	dbGroupVo__resolvedKey = __key;
            }
        }
        return dbGroupVo;
    }

    public void setDbGroupVo(DbGroupVo dbGroupVo) {
        if (dbGroupVo == null) {
            throw new DaoException("To-one property 'groupId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.dbGroupVo = dbGroupVo;
            groupId = dbGroupVo.getGroupId();
            dbGroupVo__resolvedKey = groupId;
        }
    }

    /** To-one relationship, resolved on first access. */
    public DbPlugVo getDbPlugVo() {
        String __key = this.plugId;
        if (dbPlugVo__resolvedKey == null || dbPlugVo__resolvedKey != __key) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DbPlugVoDao targetDao = daoSession.getDbPlugVoDao();
            DbPlugVo dbPlugVoNew = targetDao.load(__key);
            synchronized (this) {
                dbPlugVo = dbPlugVoNew;
            	dbPlugVo__resolvedKey = __key;
            }
        }
        return dbPlugVo;
    }

    public void setDbPlugVo(DbPlugVo dbPlugVo) {
        if (dbPlugVo == null) {
            throw new DaoException("To-one property 'plugId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.dbPlugVo = dbPlugVo;
            plugId = dbPlugVo.getPlugId();
            dbPlugVo__resolvedKey = plugId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
