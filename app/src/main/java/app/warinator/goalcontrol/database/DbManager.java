package app.warinator.goalcontrol.database;

import android.content.Context;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ConcreteTaskDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.QueuedDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;

/**
 * Created by Warinator on 01.04.2017.
 */

public class DbManager {
    private static final String DATABASE_NAME = "goals.db";
    private static final int DATABASE_VERSION  = 1;
    private static DaoManager mDaoManager;

    private DbManager(){}

    public static DaoManager getInstance(final Context context){
        if (mDaoManager == null){
            mDaoManager = DaoManager.with(context)
                    .databaseName(DATABASE_NAME)
                    .version(DATABASE_VERSION)
                    .foreignKeyConstraints(true)
                    .add(new CategoryDAO()).add(new ProjectDAO()).add( new TrackUnitDAO())
                    .add(new TaskDAO()).add(new CheckListItemDAO())
                    .add(new ConcreteTaskDAO()).add(new QueuedDAO())
                    .logging(true)
                    .build();
        }
        return mDaoManager;
    }

}
