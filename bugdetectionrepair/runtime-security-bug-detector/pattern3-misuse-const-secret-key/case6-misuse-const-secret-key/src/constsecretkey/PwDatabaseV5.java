/*
 * Copyright 2017 Brian Pellin, Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 *

Derived from

KeePass for J2ME

Copyright 2007 Naomaru Itoi <nao@phoneid.org>

This file was derived from 

Java clone of KeePass - A KeePass file viewer for Java
Copyright 2006 Bill Zwicky <billzwicky@users.sourceforge.net>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package constsecretkey;

import java.io.InputStream;

public class PwDatabaseV5 extends PwDatabase {

	private static final int DEFAULT_ENCRYPTION_ROUNDS = 300;

	@Override
	public void clearCache() {}

   @Override
   protected byte[] loadXmlKeyFile(InputStream keyInputStream) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected String getPasswordEncoding() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public long getNumberKeyEncryptionRounds() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void setNumberKeyEncryptionRounds(long rounds) throws NumberFormatException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void initNew(String dbPath) {
      // TODO Auto-generated method stub
      
   }
}
