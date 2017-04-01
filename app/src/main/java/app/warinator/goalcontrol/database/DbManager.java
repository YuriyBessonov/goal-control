package app.warinator.goalcontrol.database;

import android.content.Context;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import app.warinator.goalcontrol.database.DAO.CategoryDAO;
import app.warinator.goalcontrol.database.DAO.CheckListItemDAO;
import app.warinator.goalcontrol.database.DAO.ChronoModeDAO;
import app.warinator.goalcontrol.database.DAO.PriorityDAO;
import app.warinator.goalcontrol.database.DAO.ProjectDAO;
import app.warinator.goalcontrol.database.DAO.TaskDAO;
import app.warinator.goalcontrol.database.DAO.TrackModeDao;
import app.warinator.goalcontrol.database.DAO.TrackUnitDAO;
import app.warinator.goalcontrol.database.DAO.WeekDaysDAO;

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
                    .add(new CategoryDAO()).add(new ChronoModeDAO()).add(new PriorityDAO())
                    .add(new ProjectDAO()).add(new TrackModeDao()).add( new TrackUnitDAO())
                    .add(new WeekDaysDAO()).add(new TaskDAO()).add(new CheckListItemDAO())
                    .logging(true)
                    .build();
        }
        return mDaoManager;
    }
}
