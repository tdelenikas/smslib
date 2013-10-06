using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DummyExample
{
	class Program
	{
		static void Main(string[] args)
		{
			Console.WriteLine(org.smslib.core.Settings.LIBRARY_INFO);
			Console.WriteLine(org.smslib.core.Settings.LIBRARY_LICENSE);
			Console.WriteLine(org.smslib.core.Settings.LIBRARY_COPYRIGHT);
			Console.WriteLine(org.smslib.core.Settings.LIBRARY_VERSION);
			Console.WriteLine();
			Console.WriteLine("Press <ENTER> to exit...");
			Console.ReadLine();
		}
	}
}
