package org.polarsys.capella.vp.ms.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.table.metamodel.table.DColumn;
import org.eclipse.sirius.table.metamodel.table.DTable;
import org.eclipse.sirius.table.ui.tools.api.editor.DTableEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.vp.ms.Situation;
import org.polarsys.capella.vp.ms.expression.ag.ExcelExporter;

public class SituationExpressionExportHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    DTableEditor editor = (DTableEditor) HandlerUtil.getActiveEditor(event);
    ExcelExporter exporter = new ExcelExporter();

    DTable table = (DTable) editor.getRepresentation();
    for (DColumn c : table.getColumns()) {
      Situation s = (Situation) c.getTarget();
      if (s.getExpression() != null) {
        exporter.export(s);
      }
    }

    Session session = SessionManager.INSTANCE.getSession(table.getTarget());
    URI sessionURI = session.getSessionResource().getURI();

    // make a file that probably doesn't exist yet..
    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    String ts = format.format(new Date());
    String excel = sessionURI.trimSegments(1).appendSegment(table.getName() + "_" + ts).appendFileExtension("xlsx").toPlatformString(true);

    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(excel));

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    try {
      exporter.finish(bytes);
      ByteArrayInputStream inbb = new ByteArrayInputStream(bytes.toByteArray());
      file.create(inbb, true, new NullProgressMonitor());
    } catch (IOException | CoreException e) {
      throw new ExecutionException("IOException during export", e);
    }

    return null;
  }
}