package com.sicoms.smartplug.dao;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table tb_cutoff.
 */
public class DbCutOffVo {

    /** Not-null value. */
    private String plugId;
    private Long cutSeq;
    /** Not-null value. */
    private String setWatt;
    /** Not-null value. */
    private String setMin;
    /** Not-null value. */
    private String useYn;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient DbCutOffVoDao myDao;

    private DbPlugVo dbPlugVo;
    private String dbPlugVo__resolvedKey;


    public DbCutOffVo() {
    }

    public DbCutOffVo(Long cutSeq) {
        this.cutSeq = cutSeq;
    }

    public DbCutOffVo(String plugId, Long cutSeq, String setWatt, String setMin, String useYn) {
        this.plugId = plugId;
        this.cutSeq = cutSeq;
        this.setWatt = setWatt;
        this.setMin = setMin;
        this.useYn = useYn;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDbCutOffVoDao() : null;
    }

    /** Not-null value. */
    public String getPlugId() {
        return plugId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public Long getCutSeq() {
        return cutSeq;
    }

    public void setCutSeq(Long cutSeq) {
        this.cutSeq = cutSeq;
    }

    /** Not-null value. */
    public String getSetWatt() {
        return setWatt;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSetWatt(String setWatt) {
        this.setWatt = setWatt;
    }

    /** Not-null value. */
    public String getSetMin() {
        return setMin;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSetMin(String setMin) {
        this.setMin = setMin;
    }

    /** Not-null value. */
    public String getUseYn() {
        return useYn;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUseYn(String useYn) {
        this.useYn = useYn;
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