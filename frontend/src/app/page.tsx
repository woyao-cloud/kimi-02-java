import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-bold">User Management System</CardTitle>
          <CardDescription>
            Full-stack application with Spring Boot and Next.js
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-center text-muted-foreground">
            A complete user management solution with authentication, authorization,
            and role-based access control.
          </p>
          <div className="flex gap-4 justify-center">
            <Link href="/login" className="w-full">
              <Button className="w-full">Sign In</Button>
            </Link>
            <Link href="/register" className="w-full">
              <Button variant="outline" className="w-full">Sign Up</Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
